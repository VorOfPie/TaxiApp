package com.modsen.taxi.driversrvice.service.impl;

import com.modsen.taxi.driversrvice.domain.Car;
import com.modsen.taxi.driversrvice.dto.request.CreateCarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import com.modsen.taxi.driversrvice.error.exception.AccessDeniedException;
import com.modsen.taxi.driversrvice.error.exception.DuplicateResourceException;
import com.modsen.taxi.driversrvice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.driversrvice.mapper.CarMapper;
import com.modsen.taxi.driversrvice.repository.CarRepository;
import com.modsen.taxi.driversrvice.repository.DriverRepository;
import com.modsen.taxi.driversrvice.service.CarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;
    private final Scheduler jdbcScheduler;
    private final DriverRepository driverRepository;

    @Override
   public Mono<CarResponse> getCarById(Long id, String principalEmail, boolean isAdmin) {
        log.info("Fetching car with ID: {}", id);
        return Mono.fromCallable(() -> carRepository.findByIdAndIsDeletedFalse(id)
                        .orElseThrow(() -> {
                            log.error("Car with ID {} not found", id);
                            return new ResourceNotFoundException("Car with id: " + id + " not found");
                        }))
                .subscribeOn(jdbcScheduler)
                .map(car -> {
                    if (!isAdmin && (car.getDriver() == null || !car.getDriver().getEmail().equals(principalEmail))) {
                        throw new AccessDeniedException("You do not have permission to access this car.");
                    }
                    return carMapper.toCarResponse(car);
                });
    }

    @Override
    public Mono<CarResponse> createCar(CreateCarRequest createCarRequest) {
        log.info("Creating car with license plate: {}", createCarRequest.licensePlate());
        return Mono.fromCallable(() -> {
                    boolean exists = carRepository.existsByLicensePlate(createCarRequest.licensePlate());
                    if (exists) {
                        log.warn("Duplicate car with license plate: {}", createCarRequest.licensePlate());
                        throw new DuplicateResourceException("Car with license plate " + createCarRequest.licensePlate() + " already exists.");
                    }
                    Car car = carMapper.toCar(createCarRequest);
                    car.setIsDeleted(false);
                    Car savedCar = carRepository.save(car);
                    log.info("Car created with ID: {}", savedCar.getId());
                    return savedCar;
                })
                .subscribeOn(jdbcScheduler)
                .map(carMapper::toCarResponse);
    }

    @Override
    public Mono<CarResponse> updateCar(Long id, CreateCarRequest createCarRequest, String principalEmail, boolean isAdmin) {
        log.info("Updating car with ID: {}", id);
        return Mono.fromCallable(() -> {
                    Car car = carRepository.findByIdAndIsDeletedFalse(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Car with id: " + id + " not found"));

                    if (!isAdmin && (car.getDriver() == null || !car.getDriver().getEmail().equals(principalEmail))) {
                        throw new AccessDeniedException("You do not have permission to update this car.");
                    }

                    carMapper.updateCarFromRequest(createCarRequest, car);
                    Car updatedCar = carRepository.save(car);
                    log.info("Car updated with ID: {}", updatedCar.getId());
                    return updatedCar;
                })
                .subscribeOn(jdbcScheduler)
                .map(carMapper::toCarResponse);
    }

    @Override
     public Mono<Void> deleteCar(Long id, String principalEmail, boolean isAdmin) {
        log.info("Deleting car with ID: {}", id);
        return Mono.fromRunnable(() -> {
                    Car car = carRepository.findByIdAndIsDeletedFalse(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Car with id: " + id + " not found"));

                    if (!isAdmin && (car.getDriver() == null || !car.getDriver().getEmail().equals(principalEmail))) {
                        throw new AccessDeniedException("You do not have permission to delete this car.");
                    }
                    car.setIsDeleted(true);
                    carRepository.save(car);
                    log.info("Car with ID {} marked as deleted", id);
                })
                .subscribeOn(jdbcScheduler)
                .then();
    }

    @Override
    public Mono<Page<CarResponse>> getAllCars(Pageable pageable, String brand, String color, String licensePlate, boolean isActive) {
        log.info("Fetching all cars with filters - brand: {}, color: {}, licensePlate: {}, isActive: {}", brand, color, licensePlate, isActive);
        return Mono.fromCallable(() -> {
                    Car carProbe = Car.builder()
                            .brand(brand)
                            .color(color)
                            .licensePlate(licensePlate)
                            .isDeleted(!isActive)
                            .build();

                    ExampleMatcher matcher = ExampleMatcher.matchingAll()
                            .withIgnoreNullValues()
                            .withMatcher("brand", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                            .withMatcher("color", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                            .withMatcher("licensePlate", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

                    Example<Car> example = Example.of(carProbe, matcher);

                    Page<Car> cars = carRepository.findAll(example, pageable);
                    log.info("Fetched {} cars", cars.getTotalElements());
                    return cars.map(carMapper::toCarResponse);
                })
                .subscribeOn(jdbcScheduler);
    }

}
