package com.modsen.taxiapp.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/passengers")
    public ResponseEntity<String> passengersFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Passenger Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/drivers")
    public ResponseEntity<String> driversFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Driver Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/trips")
    public ResponseEntity<String> tripsFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Trip Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/ratings")
    public ResponseEntity<String> ratingsFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Rating Service is currently unavailable. Please try again later.");
    }
}
