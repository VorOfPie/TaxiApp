package com.modsen.taxi.driversrvice.dto.response;

import lombok.Builder;

@Builder
public record DriverResponse(
        Long id,

        String firstName,

        String lastName,

        String phone,

        String gender,

        CarResponse car
) {
}
