package com.modsen.taxi.driversrvice.service.impl;

import com.modsen.taxi.driversrvice.domain.Car;
import com.modsen.taxi.driversrvice.dto.request.CreateCarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import com.modsen.taxi.driversrvice.error.exception.DuplicateResourceException;
import com.modsen.taxi.driversrvice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.driversrvice.mapper.CarMapper;
import com.modsen.taxi.driversrvice.repository.CarRepository;
import com.modsen.taxi.driversrvice.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;
    private final Scheduler jdbcScheduler;

    @Override
    public Mono<CarResponse> getCarById(Long id) {
        return Mono.fromCallable(() -> carRepository.findByIdAndIsDeletedFalse(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + id)))
                .subscribeOn(jdbcScheduler)
                .map(carMapper::toCarResponse);
    }

    @Override
    public Mono<CarResponse> createCar(CreateCarRequest createCarRequest) {
        return Mono.fromCallable(() -> {
                    boolean exists = carRepository.existsByLicensePlate(createCarRequest.licensePlate());
                    if (exists) {
                        throw new DuplicateResourceException("Car with license plate " + createCarRequest.licensePlate() + " already exists.");
                    }
                    Car car = carMapper.toCar(createCarRequest);
                    car.setIsDeleted(false);
                    return carRepository.save(car);
                })
                .subscribeOn(jdbcScheduler)
                .map(carMapper::toCarResponse);
    }

    @Override
    public Mono<CarResponse> updateCar(Long id, CreateCarRequest createCarRequest) {
        return Mono.fromCallable(() -> {
                    Car car = carRepository.findByIdAndIsDeletedFalse(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + id));
                    carMapper.updateCarFromRequest(createCarRequest, car);
                    return carRepository.save(car);
                })
                .subscribeOn(jdbcScheduler)
                .map(carMapper::toCarResponse);
    }

    @Override
    public Mono<Void> deleteCar(Long id) {
        return Mono.fromRunnable(() -> {
                    Car car = carRepository.findByIdAndIsDeletedFalse(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + id));
                    car.setIsDeleted(true);
                    carRepository.save(car);
                })
                .subscribeOn(jdbcScheduler)
                .then();
    }

    @Override
    public Mono<Page<CarResponse>> getAllCars(Pageable pageable, String brand, String color, String licensePlate, boolean isActive) {
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
                    return cars.map(carMapper::toCarResponse);
                })
                .subscribeOn(jdbcScheduler);
    }
}
