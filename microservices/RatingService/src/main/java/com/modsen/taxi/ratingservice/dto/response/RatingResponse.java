package com.modsen.taxi.ratingservice.dto.response;

public record RatingResponse(
        Long id,
        Long driverId,
        Long passengerId,
        Double score,
        String comment
) {}
