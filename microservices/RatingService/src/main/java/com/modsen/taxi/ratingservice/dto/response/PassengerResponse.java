package com.modsen.taxi.ratingservice.dto.response;

public record PassengerResponse(
        Long id,

        String firstName,

        String lastName,

        String email,

        String phone
) {
}
