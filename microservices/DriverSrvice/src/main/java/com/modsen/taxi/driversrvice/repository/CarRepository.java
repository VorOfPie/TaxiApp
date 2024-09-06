package com.modsen.taxi.driversrvice.repository;

import com.modsen.taxi.driversrvice.domain.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findAllByIsDeletedFalse();
    Optional<Car> findByIdAndIsDeletedFalse(Long id);
}
