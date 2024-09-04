package com.modsen.taxi.driversrvice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CarRequest(
        @NotBlank(message = "Brand is required")
        @Size(min = 2, max = 50, message = "Brand must be between 2 and 50 characters")
        String brand,

        @NotBlank(message = "Color is required")
        @Size(min = 2, max = 30, message = "Color must be between 2 and 30 characters")
        String color,

        @NotBlank(message = "License plate is required")
        @Size(min = 5, max = 15, message = "License plate must be between 5 and 15 characters")
        String licensePlate
) {}
