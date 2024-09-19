package com.modsen.taxi.ratingservice.config;

import com.modsen.taxi.ratingservice.dto.response.PassengerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "passenger-service", url = "${ratingservice.urls.passenger}")
public interface PassengerClient {
    @GetMapping("/{id}")
    PassengerResponse getPassengerById(@PathVariable("id") Long id);
}