package com.modsen.taxi.ratingservice.repository;

import com.modsen.taxi.ratingservice.domain.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.driverId = :driverId")
    Optional<Double> calculateAverageRatingByDriverId(Long driverId);

    boolean existsByDriverIdAndPassengerId(Long driverId, Long passengerId);
}
