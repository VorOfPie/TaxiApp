package com.modsen.taxi.driversrvice.service;

import com.modsen.taxi.driversrvice.dto.request.CarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarResponse getCar(Long id);

    CarResponse createCar(CarRequest carRequest);

    CarResponse updateCar(Long id, CarRequest carRequest);

    void deleteCar(Long id);

    Page<CarResponse> getAllCars(Pageable pageable, String brand, String color, String licensePlate);
}
