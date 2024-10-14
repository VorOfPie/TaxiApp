package com.modsen.taxi.driversrvice.service;

import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface DriverService {

    Mono<DriverResponse> getDriverById(Long id);

    Mono<DriverResponse> createDriver(DriverRequest driverRequest);

    Mono<DriverResponse> updateDriver(Long id, DriverRequest driverRequest);

    Mono<Void> deleteDriver(Long id);

    Mono<Page<DriverResponse>> getAllDrivers(Pageable pageable, String firstName, String lastName, String phone, boolean isActive);
}
