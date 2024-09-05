package com.modsen.taxi.driversrvice.service.impl;

import com.modsen.taxi.driversrvice.domain.Driver;
import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
import com.modsen.taxi.driversrvice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.driversrvice.mapper.DriverMapper;
import com.modsen.taxi.driversrvice.repository.DriverRepository;
import com.modsen.taxi.driversrvice.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;

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
        driver.getCar().setIsDeleted(false);
        Driver savedDriver = driverRepository.save(driver);
        return driverMapper.toDriverResponse(savedDriver);
    }

    @Override
    public DriverResponse updateDriver(Long id, DriverRequest driverRequest) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));
        driverMapper.updateDriverFromRequest(driverRequest, driver);
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

        if (drivers.isEmpty()) {
            throw new ResourceNotFoundException("No drivers found with the specified filters");
        }

        return drivers.map(driverMapper::toDriverResponse);
    }

}
