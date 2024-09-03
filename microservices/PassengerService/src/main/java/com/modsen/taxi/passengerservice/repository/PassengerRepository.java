package com.modsen.taxi.passengerservice.repository;

import com.modsen.taxi.passengerservice.domain.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
}