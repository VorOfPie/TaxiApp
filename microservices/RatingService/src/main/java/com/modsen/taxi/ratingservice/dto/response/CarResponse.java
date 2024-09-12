package com.modsen.taxi.ratingservice.dto.response;

public record CarResponse(
        Long id,

        String brand,

        String color,

        String licensePlate
) {
}
