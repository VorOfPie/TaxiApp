package com.modsen.taxi.driversrvice.controller;

import com.modsen.taxi.driversrvice.dto.request.CreateCarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import com.modsen.taxi.driversrvice.service.CarService;
import com.modsen.taxi.driversrvice.swagger.CarApi;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
public class CarController implements CarApi {

    private final CarService carService;
    @Override
    @PostMapping
    public Mono<ResponseEntity<CarResponse>> createCar(@RequestBody CreateCarRequest createCarRequest) {
        return carService.createCar(createCarRequest)
                .map(car -> new ResponseEntity<>(car, HttpStatus.CREATED));
    }
    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Mono<ResponseEntity<CarResponse>> updateCar(@PathVariable Long id,
                                                       @RequestBody CreateCarRequest createCarRequest,
                                                       @AuthenticationPrincipal Jwt jwt) {
        String principalEmail = jwt.getClaim("email");
        boolean isAdmin = isAdmin(jwt);
        return carService.updateCar(id, createCarRequest, principalEmail, isAdmin)
                .map(updatedCar -> new ResponseEntity<>(updatedCar, HttpStatus.OK));
    }
    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Mono<ResponseEntity<CarResponse>> getCarById(@PathVariable Long id,
                                                        @AuthenticationPrincipal Jwt jwt) {
        String principalEmail = jwt.getClaim("email");
        boolean isAdmin = isAdmin(jwt);
        return carService.getCarById(id, principalEmail, isAdmin)
                .map(car -> new ResponseEntity<>(car, HttpStatus.OK));
    }
    @Override
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Map<String, Object>>> getAllCars(@RequestParam(required = false) String brand,
                                                                @RequestParam(required = false) String color,
                                                                @RequestParam(required = false) String licensePlate,
                                                                @RequestParam(defaultValue = "true") boolean isActive,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size,
                                                                @RequestParam(defaultValue = "id,asc") String sort) {

        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(sortParams[0]).ascending();
        if ("desc".equalsIgnoreCase(sortParams[1])) {
            sortOrder = Sort.by(sortParams[0]).descending();
        }
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        return carService.getAllCars(pageable, brand, color, licensePlate, isActive)
                .map(pageCars -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("cars", pageCars.getContent());
                    response.put("currentPage", pageCars.getNumber());
                    response.put("totalItems", pageCars.getTotalElements());
                    response.put("totalPages", pageCars.getTotalPages());
                    return new ResponseEntity<>(response, HttpStatus.OK);
                });
    }
    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Mono<ResponseEntity<Void>> deleteCar(@PathVariable Long id,
                                                @AuthenticationPrincipal Jwt jwt) {
        String principalEmail = jwt.getClaim("email");
        boolean isAdmin = isAdmin(jwt);
        return carService.deleteCar(id, principalEmail, isAdmin)
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    private boolean isAdmin(Jwt jwt) {
        var roles = (List<String>) jwt.getClaimAsMap("realm_access").get("roles");
        return roles.contains("ROLE_ADMIN");
    }
}
