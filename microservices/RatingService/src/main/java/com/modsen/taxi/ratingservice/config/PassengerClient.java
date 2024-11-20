package com.modsen.taxi.ratingservice.config;

import com.modsen.taxi.ratingservice.dto.response.PassengerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "passenger-service", configuration = FeignConfig.class)
public interface PassengerClient {
    @GetMapping("/api/v1/passengers/{id}")
    PassengerResponse getPassengerById(@PathVariable("id") Long id);
}