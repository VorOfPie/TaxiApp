package com.modsen.taxi.driversrvice.service;

import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DriverService {

    DriverResponse getDriverById(Long id);

    DriverResponse createDriver(DriverRequest driverRequest);

    DriverResponse updateDriver(Long id, DriverRequest driverRequest);

    void deleteDriver(Long id);

    Page<DriverResponse> getAllDrivers(Pageable pageable, String firstName, String lastName, String phone,  boolean isActive);
}
