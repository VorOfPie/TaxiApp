package com.modsen.taxi.driversrvice.service;

import com.modsen.taxi.driversrvice.dto.request.CreateCarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface CarService {
    Mono<CarResponse> getCarById(Long id);

    Mono<CarResponse> createCar(CreateCarRequest createCarRequest);

    Mono<CarResponse> updateCar(Long id, CreateCarRequest createCarRequest);

    Mono<Void> deleteCar(Long id);

    Mono<Page<CarResponse>> getAllCars(Pageable pageable, String brand, String color, String licensePlate, boolean isActive);
}
