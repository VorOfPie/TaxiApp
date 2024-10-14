package com.modsen.taxi.passengerservice.repository;

import com.modsen.taxi.passengerservice.domain.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    Optional<Passenger> findByIdAndIsDeletedFalse(Long id);

    List<Passenger> findAllByIsDeletedFalse();

    boolean existsByEmail(String email);

    Optional<Passenger> findByEmailAndIsDeletedFalse(String email);

    void deletePassengerByEmail(String email);

    void deleteByEmail(String email);
}