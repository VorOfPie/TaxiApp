package com.modsen.taxi.driversrvice.service.impl;

import com.modsen.taxi.driversrvice.domain.Car;
import com.modsen.taxi.driversrvice.dto.request.CarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import com.modsen.taxi.driversrvice.mapper.CarMapper;
import com.modsen.taxi.driversrvice.repository.CarRepository;
import com.modsen.taxi.driversrvice.service.CarService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public CarResponse getCar(Long id) {
        Car car = carRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Car not found"));
        return carMapper.toCarResponse(car);
    }

    @Override
    public CarResponse createCar(CarRequest carRequest) {
        Car car = carMapper.toCar(carRequest);
        Car savedCar = carRepository.save(car);
        return carMapper.toCarResponse(savedCar);
    }

    @Override
    public CarResponse updateCar(Long id, CarRequest carRequest) {
        Car car = carRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Car not found"));
        carMapper.updateCarFromRequest(carRequest, car);
        Car updatedCar = carRepository.save(car);
        return carMapper.toCarResponse(updatedCar);
    }

    @Override
    public void deleteCar(Long id) {
        Car car = carRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Car not found"));
        car.setIsDeleted(true);
        carRepository.save(car);
    }

    @Override
    public Page<CarResponse> getAllCars(Pageable pageable, String brand, String color, String licensePlate) {
        Car carProbe = Car.builder()
                .brand(brand)
                .color(color)
                .licensePlate(licensePlate)
                .isDeleted(false)
                .build();

        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreNullValues()
                .withMatcher("brand", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("color", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("licensePlate", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

        Example<Car> example = Example.of(carProbe, matcher);

        Page<Car> cars = carRepository.findAll(example, pageable);

        return cars.map(carMapper::toCarResponse);
    }
}
