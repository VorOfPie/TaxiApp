package com.modsen.taxi.driversrvice.repository;

import com.modsen.taxi.driversrvice.domain.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    List<Driver> findAllByIsDeletedFalse();
}
