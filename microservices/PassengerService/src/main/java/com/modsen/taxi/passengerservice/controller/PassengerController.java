package com.modsen.taxi.passengerservice.controller;

import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;
import com.modsen.taxi.passengerservice.service.PassengerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/passengers")
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerService passengerService;

    @PostMapping
    public Mono<ResponseEntity<PassengerResponse>> createPassenger(@Valid @RequestBody PassengerRequest passengerRequest) {
        return passengerService.createPassenger(passengerRequest)
                .map(passenger -> new ResponseEntity<>(passenger, HttpStatus.CREATED));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<PassengerResponse>> updatePassenger(@PathVariable Long id, @Valid @RequestBody PassengerRequest passengerRequest) {
        return passengerService.updatePassenger(id, passengerRequest)
                .map(updatedPassenger -> new ResponseEntity<>(updatedPassenger, HttpStatus.OK));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<PassengerResponse>> getPassengerById(@PathVariable Long id) {
        return passengerService.getPassengerById(id)
                .map(passenger -> new ResponseEntity<>(passenger, HttpStatus.OK));
    }

    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> getAllPassengers(@RequestParam(required = false) String firstName,
                                                                      @RequestParam(required = false) String lastName,
                                                                      @RequestParam(required = false) String email,
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

        return passengerService.getAllPassengers(pageable, firstName, lastName, email, isActive)
                .map(pagePassengers -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("passengers", pagePassengers.getContent());
                    response.put("currentPage", pagePassengers.getNumber());
                    response.put("totalItems", pagePassengers.getTotalElements());
                    response.put("totalPages", pagePassengers.getTotalPages());
                    return new ResponseEntity<>(response, HttpStatus.OK);
                });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deletePassenger(@PathVariable Long id) {
        return passengerService.deletePassenger(id)
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }
}
