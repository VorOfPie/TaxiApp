package com.modsen.taxi.driversrvice.service;

import com.modsen.taxi.driversrvice.dto.request.CreateCarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarResponse getCarById(Long id);

    CarResponse createCar(CreateCarRequest createCarRequest);

    CarResponse updateCar(Long id, CreateCarRequest createCarRequest);

    void deleteCar(Long id);

    Page<CarResponse> getAllCars(Pageable pageable, String brand, String color, String licensePlate);
}
