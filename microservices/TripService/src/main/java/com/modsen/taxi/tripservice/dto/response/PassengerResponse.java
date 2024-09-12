package com.modsen.taxi.tripservice.dto.response;

public record PassengerResponse(
        Long id,

        String firstName,

        String lastName,

        String email,

        String phone
) {
}
