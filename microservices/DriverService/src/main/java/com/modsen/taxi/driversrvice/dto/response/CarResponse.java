package com.modsen.taxi.driversrvice.dto.response;

public record CarResponse(
        Long id,

        String brand,

        String color,

        String licensePlate
) {
}
