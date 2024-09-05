package com.modsen.taxi.passengerservice.controller;

import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;
import com.modsen.taxi.passengerservice.service.PassengerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/passengers")
@Validated
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerService passengerService;

    @PostMapping
    public ResponseEntity<PassengerResponse> createPassenger(@Valid @RequestBody PassengerRequest passengerRequest) {
        PassengerResponse createdPassenger = passengerService.createPassenger(passengerRequest);
        return new ResponseEntity<>(createdPassenger, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PassengerResponse> updatePassenger(
            @PathVariable Long id,
            @Valid @RequestBody PassengerRequest passengerRequest) {
        PassengerResponse updatedPassenger = passengerService.updatePassenger(id, passengerRequest);
        return new ResponseEntity<>(updatedPassenger, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassengerResponse> getPassengerById(@PathVariable Long id) {
        PassengerResponse passengerResponse = passengerService.getPassengerById(id);
        return new ResponseEntity<>(passengerResponse, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPassengers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "true") boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        try {
            String[] sortParams = sort.split(",");
            Sort sortOrder = Sort.by(sortParams[0]).ascending();
            if ("desc".equalsIgnoreCase(sortParams[1])) {
                sortOrder = Sort.by(sortParams[0]).descending();
            }
            Pageable pageable = PageRequest.of(page, size, sortOrder);
            Page<PassengerResponse> pagePassengers = passengerService.getAllPassengers(pageable, firstName, lastName, email, isActive);

            Map<String, Object> response = new HashMap<>();
            response.put("passengers", pagePassengers.getContent());
            response.put("currentPage", pagePassengers.getNumber());
            response.put("totalItems", pagePassengers.getTotalElements());
            response.put("totalPages", pagePassengers.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePassenger(@PathVariable Long id) {
        passengerService.deletePassenger(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
