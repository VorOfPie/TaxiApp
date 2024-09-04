package com.modsen.taxi.passengerservice.controller;

import com.modsen.taxi.passengerservice.service.PassengerService;
import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable paging = PageRequest.of(page, size);
            Page<PassengerResponse> pagePassengers = passengerService.getAllPassengers(paging, firstName, lastName, email);

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
