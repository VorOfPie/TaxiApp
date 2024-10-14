package com.modsen.taxi.tripservice.repository;

import com.modsen.taxi.tripservice.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    boolean existsByDriverIdAndPassengerId(Long driverId, Long passengerId);

    Optional<Trip> findByDriverIdAndPassengerId(Long driverId, Long passengerId);
}
