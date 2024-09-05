package com.modsen.taxi.driversrvice.service.impl;

import com.modsen.taxi.driversrvice.domain.Car;
import com.modsen.taxi.driversrvice.dto.request.CreateCarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import com.modsen.taxi.driversrvice.error.exception.ResourceNotFoundException;
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
    public CarResponse getCarById(Long id) {
        Car car = carRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + id));
        return carMapper.toCarResponse(car);
    }

    @Override
    public CarResponse createCar(CreateCarRequest createCarRequest) {
        Car car = carMapper.toCar(createCarRequest);
        car.setIsDeleted(false);
        Car savedCar = carRepository.save(car);
        return carMapper.toCarResponse(savedCar);
    }

    @Override
    public CarResponse updateCar(Long id, CreateCarRequest createCarRequest) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + id));
        carMapper.updateCarFromRequest(createCarRequest, car);
        Car updatedCar = carRepository.save(car);
        return carMapper.toCarResponse(updatedCar);
    }

    @Override
    public void deleteCar(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + id));
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

        if (cars.isEmpty()) {
            throw new ResourceNotFoundException("No cars found with the specified filters");
        }

        return cars.map(carMapper::toCarResponse);
    }
}
