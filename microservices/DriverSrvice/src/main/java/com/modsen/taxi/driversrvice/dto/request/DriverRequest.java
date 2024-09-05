// DriverRequest.java
package com.modsen.taxi.driversrvice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DriverRequest(

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "\\+?[0-9\\-\\s]+", message = "Phone number should be valid")
        String phone,

        @NotBlank(message = "Gender is required")
        String gender,
        List<CarRequest> cars
) {
}
