package com.modsen.taxi.tripservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TripRequest(
        @NotNull(message = "Driver ID cannot be null")
        Long driverId,

        @NotNull(message = "Passenger ID cannot be null")
        Long passengerId,

        @NotBlank(message = "Origin address cannot be blank")
        String originAddress,

        @NotBlank(message = "Destination address cannot be blank")
        String destinationAddress,

        @NotNull(message = "Price cannot be null")
        @Positive(message = "Price must be greater than zero")
        BigDecimal price
) {}
