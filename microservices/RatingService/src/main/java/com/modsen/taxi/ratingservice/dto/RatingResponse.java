package com.modsen.taxi.ratingservice.dto;

public record RatingResponse(
        Long id,
        Long driverId,
        Long passengerId,
        Double score,
        String comment
) {}
