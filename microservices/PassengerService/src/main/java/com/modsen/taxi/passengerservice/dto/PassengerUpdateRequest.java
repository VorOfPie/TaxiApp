package com.modsen.taxi.passengerservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PassengerUpdateRequest(

        @Size(max = 50, message = "First name must be less than 50 characters")
        String firstName,

        @Size(max = 50, message = "Last name must be less than 50 characters")
        String lastName,

        @Email(message = "Email should be valid")
        @Size(max = 100, message = "Email must be less than 100 characters")
        String email,

        @Pattern(regexp = "\\+?[0-9\\-\\s]+", message = "Phone number should be valid")
        @Size(max = 20, message = "Phone number must be less than 20 characters")
        String phone
) {
}
