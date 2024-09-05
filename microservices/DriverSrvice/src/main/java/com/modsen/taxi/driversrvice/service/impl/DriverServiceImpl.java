package com.modsen.taxi.driversrvice.service.impl;

import com.modsen.taxi.driversrvice.domain.Car;
import com.modsen.taxi.driversrvice.domain.Driver;
import com.modsen.taxi.driversrvice.dto.request.CarRequest;
import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;

    private final CarRepository carRepository;

    @Override
    public DriverResponse getDriverById(Long id) {
        Driver driver = driverRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));
        return driverMapper.toDriverResponse(driver);
    }


    @Override
    public DriverResponse createDriver(DriverRequest driverRequest) {
        Driver driver = driverMapper.toDriver(driverRequest);
        driver.setIsDeleted(false);

        Driver savedDriver = driverRepository.save(driver);

        List<Long> carIds = driverRequest.cars().stream()
                .map(CarRequest::id)
                .collect(Collectors.toList());

        List<Car> existingCars = carRepository.findAllById(carIds);

        existingCars.forEach(car -> car.setDriver(savedDriver));

        List<Car> newCars = driverMapper.carRequestsToCars(driverRequest.cars()).stream()
                .filter(car -> car.getId() == null || !carIds.contains(car.getId()))
                .peek(car -> car.setDriver(savedDriver))
                .collect(Collectors.toList());

        List<Car> savedNewCars = carRepository.saveAll(newCars);

        carRepository.saveAll(existingCars);

        List<Car> allCars = Stream.concat(existingCars.stream(), savedNewCars.stream())
                .collect(Collectors.toList());

        savedDriver.setCars(allCars);

        Driver finalSavedDriver = driverRepository.save(savedDriver);

        return driverMapper.toDriverResponse(finalSavedDriver);
    }


    @Override
    public DriverResponse updateDriver(Long id, DriverRequest driverRequest) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));

        driverMapper.updateDriverFromRequest(driverRequest, driver);

        List<Long> carIds = driverRequest.cars().stream()
                .map(CarRequest::id)
                .collect(Collectors.toList());

        List<Car> existingCars = carRepository.findAllById(carIds);
        List<Car> newCars = driverMapper.carRequestsToCars(driverRequest.cars()).stream()
                .filter(car -> car.getId() == null || !carIds.contains(car.getId()))
                .peek(car -> car.setDriver(driver))
                .collect(Collectors.toList());

        existingCars.forEach(car -> car.setDriver(driver));
        carRepository.saveAll(existingCars);

        List<Car> savedCars = carRepository.saveAll(newCars);

        driver.setCars(Stream.concat(existingCars.stream(), savedCars.stream()).collect(Collectors.toList()));

        Driver updatedDriver = driverRepository.save(driver);
        return driverMapper.toDriverResponse(updatedDriver);
    }


    @Override
    public void deleteDriver(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));
        driver.setIsDeleted(true);
        driverRepository.save(driver);
    }

    @Override
    public Page<DriverResponse> getAllDrivers(Pageable pageable, String firstName, String lastName, String phone, boolean isActive) {
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
    }

}
