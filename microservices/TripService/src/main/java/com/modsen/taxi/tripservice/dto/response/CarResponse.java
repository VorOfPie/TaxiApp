package com.modsen.taxi.tripservice.dto.response;

public record CarResponse(
        Long id,

        String brand,

        String color,

        String licensePlate
) {
}
