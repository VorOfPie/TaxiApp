package com.modsen.taxi.passengerservice.controller;

import com.modsen.taxi.passengerservice.service.PassengerService;
import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<PassengerResponse>> getAllPassengers() {
        List<PassengerResponse> passengers = passengerService.getAllPassengers();
        return new ResponseEntity<>(passengers, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePassenger(@PathVariable Long id) {
        passengerService.deletePassenger(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
