package com.modsen.taxi.passengerservice.dto;

public record PassengerResponse(
        Long id,

        String firstName,

        String lastName,

        String email,

        String phone
) {
}
