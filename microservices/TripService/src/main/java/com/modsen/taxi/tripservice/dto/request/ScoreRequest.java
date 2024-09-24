package com.modsen.taxi.tripservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ScoreRequest(

        @NotNull(message = "Rating cannot be null")
        @Min(value = 1, message = "Rating must be at least 1.0")
        @Max(value = 5, message = "Rating must be at most 5.0")
        Double score,

        @Size(max = 500, message = "Comment cannot exceed 500 characters")
        String comment
) {
}
