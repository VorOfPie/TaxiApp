package com.modsen.taxi.ratingservice.service;

import com.modsen.taxi.ratingservice.dto.RatingRequest;
import com.modsen.taxi.ratingservice.dto.response.RatingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RatingService {

    RatingResponse createRating(RatingRequest ratingRequest);

    RatingResponse updateRating(Long id, RatingRequest ratingRequest);

    RatingResponse getRatingById(Long id);

    Page<RatingResponse> getAllRatings(Pageable pageable, Long driverId, Long passengerId);

    void deleteRating(Long id);

    Double getAverageRatingForDriver(Long driverId);
}
