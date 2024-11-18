package com.modsen.taxi.tripservice.config;

import com.modsen.taxi.tripservice.dto.response.PassengerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "passenger-service")
public interface PassengerClient {
    @GetMapping("/api/v1/passengers/{id}")
    PassengerResponse getPassengerById(@PathVariable("id") Long id);
}