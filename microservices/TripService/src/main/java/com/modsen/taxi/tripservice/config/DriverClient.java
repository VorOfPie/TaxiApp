package com.modsen.taxi.tripservice.config;

import com.modsen.taxi.tripservice.dto.response.DriverResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "driver-service")
public interface DriverClient {
    @GetMapping("/api/v1/drivers/{id}")
    DriverResponse getDriverById(@PathVariable("id") Long id);
}