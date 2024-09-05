package com.modsen.taxi.driversrvice.controller;

import com.modsen.taxi.driversrvice.dto.request.CreateCarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import com.modsen.taxi.driversrvice.service.CarService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cars")
@Validated
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCars(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String licensePlate,
            @RequestParam(required = false, defaultValue = "true") boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sort[0]).ascending());
            if ("desc".equalsIgnoreCase(sort[1])) {
                pageable = PageRequest.of(page, size, Sort.by(sort[0]).descending());
            }

            Page<CarResponse> pageCars = carService.getAllCars(pageable, brand, color, licensePlate, isActive);

            Map<String, Object> response = new HashMap<>();
            response.put("cars", pageCars.getContent());
            response.put("currentPage", pageCars.getNumber());
            response.put("totalItems", pageCars.getTotalElements());
            response.put("totalPages", pageCars.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> getCarById(@PathVariable Long id) {

        CarResponse carResponse = carService.getCarById(id);
        return new ResponseEntity<>(carResponse, HttpStatus.OK);

    }

    @PostMapping
    public ResponseEntity<CarResponse> createCar(@Validated @RequestBody CreateCarRequest createCarRequest) {
        try {
            CarResponse carResponse = carService.createCar(createCarRequest);
            return new ResponseEntity<>(carResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarResponse> updateCar(
            @PathVariable Long id,
            @Validated @RequestBody CreateCarRequest createCarRequest) {

        CarResponse updatedCar = carService.updateCar(id, createCarRequest);
        return new ResponseEntity<>(updatedCar, HttpStatus.OK);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteCar(@PathVariable Long id) {

        carService.deleteCar(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }
}
