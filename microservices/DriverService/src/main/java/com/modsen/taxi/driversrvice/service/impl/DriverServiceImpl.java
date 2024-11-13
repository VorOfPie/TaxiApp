package com.modsen.taxi.driversrvice.service.impl;

import com.modsen.taxi.driversrvice.domain.Car;
import com.modsen.taxi.driversrvice.domain.Driver;
import com.modsen.taxi.driversrvice.dto.request.CarRequest;
import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
import com.modsen.taxi.driversrvice.error.exception.DuplicateResourceException;
import com.modsen.taxi.driversrvice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.driversrvice.mapper.DriverMapper;
import com.modsen.taxi.driversrvice.repository.CarRepository;
import com.modsen.taxi.driversrvice.repository.DriverRepository;
import com.modsen.taxi.driversrvice.service.DriverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;
    private final CarRepository carRepository;
    private final Scheduler jdbcScheduler;

    @Override
    public Mono<DriverResponse> getDriverById(Long id) {
        log.info("Fetching driver with ID: {}", id);
        return Mono.fromCallable(() -> driverRepository.findByIdAndIsDeletedFalse(id)
                        .orElseThrow(() -> {
                            log.error("Driver with ID {} not found", id);
                            return new ResourceNotFoundException("Driver with id " + id + " not found");
                        }))
                .subscribeOn(jdbcScheduler)
                .map(driverMapper::toDriverResponse);
    }

    @Override
    public Mono<DriverResponse> createDriver(DriverRequest driverRequest) {
        log.info("Creating driver with phone number: {}", driverRequest.phone());
        return Mono.fromCallable(() -> {
            if (driverRepository.existsByPhone(driverRequest.phone())) {
                log.warn("Driver with phone number {} already exists", driverRequest.phone());
                throw new DuplicateResourceException("Driver with phone number " + driverRequest.phone() + " already exists.");
            }

            Driver driver = driverMapper.toDriver(driverRequest);
            driver.setIsDeleted(false);
            validateNewCars(driverRequest.cars());

            Driver savedDriver = driverRepository.save(driver);
            log.info("Driver created with ID: {}", savedDriver.getId());

            List<Car> associatedCars = associateCarsWithDriver(driverRequest.cars(), savedDriver);
            savedDriver.setCars(associatedCars);
            Driver finalSavedDriver = driverRepository.save(savedDriver);

            log.info("Driver {} associated with cars: {}", finalSavedDriver.getId(),
                    associatedCars.stream().map(Car::getLicensePlate).collect(Collectors.toList()));

            return driverMapper.toDriverResponse(finalSavedDriver);
        }).subscribeOn(jdbcScheduler);
    }

    private void validateNewCars(List<CarRequest> carRequests) {
        List<Car> cars = driverMapper.carRequestsToCars(carRequests);
        for (Car newCar : cars) {
            if (newCar.getId() == null && carRepository.existsByLicensePlate(newCar.getLicensePlate())) {
                log.warn("Car with license plate {} already exists", newCar.getLicensePlate());
                throw new DuplicateResourceException("Car with license plate " + newCar.getLicensePlate() + " already exists.");
            }
        }
        log.info("All new cars validated successfully.");
    }

    private List<Car> associateCarsWithDriver(List<CarRequest> carRequests, Driver savedDriver) {
        log.info("Associating cars with driver ID: {}", savedDriver.getId());

        List<Long> carIds = carRequests.stream()
                .map(CarRequest::id)
                .collect(Collectors.toList());

        List<Car> existingCars = carRepository.findAllById(carIds);
        existingCars.forEach(car -> car.setDriver(savedDriver));

        List<Car> newCars = driverMapper.carRequestsToCars(carRequests).stream()
                .filter(car -> car.getId() == null || !carIds.contains(car.getId()))
                .peek(car -> car.setDriver(savedDriver))
                .peek(car -> car.setIsDeleted(false))
                .collect(Collectors.toList());

        List<Car> savedNewCars = carRepository.saveAll(newCars);
        carRepository.saveAll(existingCars);

        log.info("Cars associated with driver {}: {}", savedDriver.getId(),
                Stream.concat(existingCars.stream(), savedNewCars.stream())
                        .map(Car::getLicensePlate)
                        .collect(Collectors.toList()));

        return Stream.concat(existingCars.stream(), savedNewCars.stream())
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public Mono<DriverResponse> updateDriver(Long id, DriverRequest driverRequest) {
        log.info("Updating driver with ID: {}", id);
        return Mono.fromCallable(() -> {
            Driver driver = driverRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Driver with ID {} not found", id);
                        return new ResourceNotFoundException("Driver with id " + id + " not found");
                    });

            driverMapper.updateDriverFromRequest(driverRequest, driver);

            List<Car> associatedCars = associateCarsWithDriver(driverRequest.cars(), driver);

            driver.setCars(associatedCars);
            Driver updatedDriver = driverRepository.save(driver);

            log.info("Driver {} updated successfully", id);
            return driverMapper.toDriverResponse(updatedDriver);
        }).subscribeOn(jdbcScheduler);
    }

    @Override
    public Mono<Void> deleteDriver(Long id) {
        log.info("Deleting driver with ID: {}", id);
        return Mono.fromRunnable(() -> {
                    Driver driver = driverRepository.findById(id)
                            .orElseThrow(() -> {
                                log.error("Driver with ID {} not found", id);
                                return new ResourceNotFoundException("Driver with id " + id + " not found");
                            });
                    driver.setIsDeleted(true);
                    driverRepository.save(driver);
                    log.info("Driver with ID {} marked as deleted", id);
                })
                .subscribeOn(jdbcScheduler)
                .then();
    }

    @Override
    public Mono<Page<DriverResponse>> getAllDrivers(Pageable pageable, String firstName, String lastName, String phone, boolean isActive) {
        log.info("Fetching drivers with filter - firstName: {}, lastName: {}, phone: {}, isActive: {}",
                firstName, lastName, phone, isActive);

        return Mono.fromCallable(() -> {
                    Driver driverProbe = Driver.builder()
                            .firstName(firstName)
                            .lastName(lastName)
                            .phone(phone)
                            .isDeleted(!isActive)
                            .build();

                    ExampleMatcher matcher = ExampleMatcher.matchingAll()
                            .withIgnoreNullValues()
                            .withMatcher("firstName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                            .withMatcher("lastName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                            .withMatcher("phone", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

                    Example<Driver> example = Example.of(driverProbe, matcher);

                    Page<Driver> drivers = driverRepository.findAll(example, pageable);
                    log.info("Found {} drivers", drivers.getTotalElements());

                    return drivers.map(driverMapper::toDriverResponse);
                })
                .subscribeOn(jdbcScheduler);
    }
}
