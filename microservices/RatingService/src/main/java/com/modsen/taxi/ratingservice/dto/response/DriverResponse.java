package com.modsen.taxi.ratingservice.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record DriverResponse(
        Long id,

        String firstName,

        String lastName,

        String phone,

        String gender,

        List<CarResponse> cars
) {
}
