package com.modsen.taxi.ratingservice.controller;

import com.modsen.taxi.ratingservice.dto.RatingRequest;
import com.modsen.taxi.ratingservice.dto.RatingResponse;
import com.modsen.taxi.ratingservice.service.RatingService;
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
@RequestMapping("/api/v1/rating")
@Validated
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<RatingResponse> createRating(@Valid @RequestBody RatingRequest ratingRequest) {
        RatingResponse createdRating = ratingService.createRating(ratingRequest);
        return new ResponseEntity<>(createdRating, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RatingResponse> updateRating(@PathVariable Long id, @Valid @RequestBody RatingRequest ratingRequest) {
        RatingResponse updatedRating = ratingService.updateRating(id, ratingRequest);
        return new ResponseEntity<>(updatedRating, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RatingResponse> getRatingById(@PathVariable Long id) {
        RatingResponse ratingResponse = ratingService.getRatingById(id);
        return new ResponseEntity<>(ratingResponse, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRatings(
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Long passengerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {

        try {
            String[] sortParams = sort.split(",");
            Sort sortOrder = Sort.by(sortParams[0]).ascending();
            if ("desc".equalsIgnoreCase(sortParams[1])) {
                sortOrder = Sort.by(sortParams[0]).descending();
            }
            Pageable pageable = PageRequest.of(page, size, sortOrder);
            Page<RatingResponse> pageRatings = ratingService.getAllRatings(pageable, driverId, passengerId);

            Map<String, Object> response = new HashMap<>();
            response.put("ratings", pageRatings.getContent());
            response.put("currentPage", pageRatings.getNumber());
            response.put("totalItems", pageRatings.getTotalElements());
            response.put("totalPages", pageRatings.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{driverId}/average")
    public ResponseEntity<Double> getAverageRatingForDriver(@PathVariable Long driverId) {
        Double averageRating = ratingService.getAverageRatingForDriver(driverId);
        return new ResponseEntity<>(averageRating, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable Long id) {
        ratingService.deleteRating(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
