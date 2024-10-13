package com.modsen.taxi.tripservice.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TripRequest(
        @NotNull(message = "Driver ID cannot be null")
        @Positive(message = "Driver ID must be positive")
        Long driverId,

        @NotNull(message = "Passenger ID cannot be null")
        @Positive(message = "Passenger ID must be positive")
        Long passengerId,

        @NotBlank(message = "Origin address cannot be blank")
        @Size(max = 255, message = "Origin address must be less than 255 characters")
        String originAddress,

        @NotBlank(message = "Destination address cannot be blank")
        @Size(max = 255, message = "Destination address must be less than 255 characters")
        String destinationAddress,

        @NotNull(message = "Status cannot be null")
        @Pattern(regexp = "^(CREATED|IN_PROGRESS|COMPLETED|CANCELLED)$", message = "Status must be one of: CREATED, IN_PROGRESS, COMPLETED, CANCELLED")
        String status,

        @NotNull(message = "Order date time cannot be null")
        @PastOrPresent(message = "Order date time must be in the past or present")
        LocalDateTime orderDateTime,

        @NotNull(message = "Price cannot be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price must have up to 10 digits before the decimal and 2 digits after")
        BigDecimal price
) {
}
