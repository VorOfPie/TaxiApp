package com.modsen.taxi.tripservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TripResponse(
        Long id,

        Long driverId,

        Long passengerId,

        String originAddress,

        String destinationAddress,

        String status,

        LocalDateTime orderDateTime,

        BigDecimal price
) {
}