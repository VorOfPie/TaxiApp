package com.modsen.taxi.driversrvice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DriverUpdateRequest(

        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,

        @Pattern(regexp = "\\+?[0-9\\-\\s]+", message = "Phone number should be valid")
        String phone,

        @Email(message = "Email should be valid")
        String email,

        String gender,

        List<CarRequest> cars
) {
}
