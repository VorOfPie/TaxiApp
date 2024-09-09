package com.modsen.taxi.ratingservice.repository;

import com.modsen.taxi.ratingservice.domain.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {
}
