package com.modsen.taxi.tripservice.repository;

import com.modsen.taxi.tripservice.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
}
