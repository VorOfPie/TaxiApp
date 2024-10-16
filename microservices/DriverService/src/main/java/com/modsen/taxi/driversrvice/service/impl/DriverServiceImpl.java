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

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;
    private final CarRepository carRepository;
    private final Scheduler jdbcScheduler;

    @Override
    public Mono<DriverResponse> getDriverById(Long id) {
        return Mono.fromCallable(() -> driverRepository.findByIdAndIsDeletedFalse(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id)))
                .subscribeOn(jdbcScheduler)
                .map(driverMapper::toDriverResponse);
    }

    @Override
    public Mono<DriverResponse> createDriver(DriverRequest driverRequest) {
        return Mono.fromCallable(() -> {
            if (driverRepository.existsByPhone(driverRequest.phone()))
                throw new DuplicateResourceException("Driver with phone number " + driverRequest.phone() + " already exists.");
            Driver driver = driverMapper.toDriver(driverRequest);
            driver.setIsDeleted(false);
            validateNewCars(driverRequest.cars());

            Driver savedDriver = driverRepository.save(driver);
            List<Car> associatedCars = associateCarsWithDriver(driverRequest.cars(), savedDriver);

            savedDriver.setCars(associatedCars);
            Driver finalSavedDriver = driverRepository.save(savedDriver);

            return driverMapper.toDriverResponse(finalSavedDriver);


        }).subscribeOn(jdbcScheduler);
    }

    private void validateNewCars(List<CarRequest> carRequests) {
        List<Car> cars = driverMapper.carRequestsToCars(carRequests);
        for (Car newCar : cars) {
            if (newCar.getId() == null && carRepository.existsByLicensePlate(newCar.getLicensePlate())) {
                throw new DuplicateResourceException("Car with license plate " + newCar.getLicensePlate() + " already exists.");
            }
        }
    }


    private List<Car> associateCarsWithDriver(List<CarRequest> carRequests, Driver savedDriver) {
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

        return Stream.concat(existingCars.stream(), savedNewCars.stream())
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public Mono<DriverResponse> updateDriver(Long id, DriverRequest driverRequest) {
        return Mono.fromCallable(() -> {
            Driver driver = driverRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));

            driverMapper.updateDriverFromRequest(driverRequest, driver);

            List<Car> associatedCars = associateCarsWithDriver(driverRequest.cars(), driver);

            driver.setCars(associatedCars);
            Driver updatedDriver = driverRepository.save(driver);

            return driverMapper.toDriverResponse(updatedDriver);
        }).subscribeOn(jdbcScheduler);
    }


    @Override
    public Mono<Void> deleteDriver(Long id) {
        return Mono.fromRunnable(() -> {
                    Driver driver = driverRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));
                    driver.setIsDeleted(true);
                    driverRepository.save(driver);
                })
                .subscribeOn(jdbcScheduler)
                .then();
    }

    @Override
    public Mono<Page<DriverResponse>> getAllDrivers(Pageable pageable, String firstName, String lastName, String phone, boolean isActive) {
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
                    return drivers.map(driverMapper::toDriverResponse);
                })
                .subscribeOn(jdbcScheduler);
    }
}
