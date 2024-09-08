package com.modsen.taxi.tripservice.controller;

import com.modsen.taxi.tripservice.dto.TripRequest;
import com.modsen.taxi.tripservice.dto.TripResponse;
import com.modsen.taxi.tripservice.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(@Valid @RequestBody TripRequest tripRequest) {
        TripResponse createdTrip = tripService.createTrip(tripRequest);
        return new ResponseEntity<>(createdTrip, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripResponse> updateTrip(@PathVariable Long id, @Valid @RequestBody TripRequest tripRequest) {
        TripResponse updatedTrip = tripService.updateTrip(id, tripRequest);
        return new ResponseEntity<>(updatedTrip, HttpStatus.OK);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TripResponse> updateTripStatus(@PathVariable Long id, @RequestParam String status) {
        TripResponse updatedTrip = tripService.updateTripStatus(id, status);
        return new ResponseEntity<>(updatedTrip, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTripById(@PathVariable Long id) {
        TripResponse tripResponse = tripService.getTripById(id);
        return new ResponseEntity<>(tripResponse, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTrips(
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Long passengerId,
            @RequestParam(required = false) String originAddress,
            @RequestParam(required = false) String destinationAddress,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(sortParams[0]).ascending();
        if ("desc".equalsIgnoreCase(sortParams[1])) {
            sortOrder = Sort.by(sortParams[0]).descending();
        }
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<TripResponse> pageTrips = tripService.getAllTrips(pageable, driverId, passengerId, originAddress, destinationAddress, status);

        Map<String, Object> response = new HashMap<>();
        response.put("trips", pageTrips.getContent());
        response.put("currentPage", pageTrips.getNumber());
        response.put("totalItems", pageTrips.getTotalElements());
        response.put("totalPages", pageTrips.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        tripService.deleteTrip(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
