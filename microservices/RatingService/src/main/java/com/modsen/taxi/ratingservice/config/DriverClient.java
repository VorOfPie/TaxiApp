package com.modsen.taxi.ratingservice.config;

import com.modsen.taxi.ratingservice.dto.response.DriverResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "driver-service", url = "${ratingservice.urls.driver}")
public interface DriverClient {
    @GetMapping("/{id}")
    DriverResponse getDriverById(@PathVariable("id") Long id);
}