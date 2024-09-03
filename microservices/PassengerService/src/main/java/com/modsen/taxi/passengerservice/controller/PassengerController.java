package com.modsen.taxi.passengerservice.controller;

import com.modsen.taxi.passengerservice.service.PassengerService;
import dto.PassengerDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/passengers")
@Validated
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerService passengerService;


    @PostMapping
    public ResponseEntity<PassengerDTO> createPassenger(@Valid @RequestBody PassengerDTO passengerDTO) {
        PassengerDTO createdPassenger = passengerService.createPassenger(passengerDTO);
        return new ResponseEntity<>(createdPassenger, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PassengerDTO> updatePassenger(
            @PathVariable Long id,
            @Valid @RequestBody PassengerDTO passengerDTO) {
        Optional<PassengerDTO> updatedPassenger = passengerService.updatePassenger(id, passengerDTO);
        return updatedPassenger
                .map(passenger -> new ResponseEntity<>(passenger, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassengerDTO> getPassengerById(@PathVariable Long id) {
        Optional<PassengerDTO> passengerDTO = passengerService.getPassengerById(id);
        return passengerDTO
                .map(passenger -> new ResponseEntity<>(passenger, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<PassengerDTO>> getAllPassengers() {
        List<PassengerDTO> passengers = passengerService.getAllPassengers();
        return new ResponseEntity<>(passengers, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePassenger(@PathVariable Long id) {
        passengerService.deletePassenger(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}