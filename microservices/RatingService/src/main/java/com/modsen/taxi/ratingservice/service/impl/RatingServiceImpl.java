package com.modsen.taxi.ratingservice.service.impl;

import com.modsen.taxi.ratingservice.config.DriverClient;
import com.modsen.taxi.ratingservice.config.PassengerClient;
import com.modsen.taxi.ratingservice.domain.Rating;
import com.modsen.taxi.ratingservice.dto.RatingRequest;
import com.modsen.taxi.ratingservice.dto.response.RatingResponse;
import com.modsen.taxi.ratingservice.error.exception.DuplicateResourceException;
import com.modsen.taxi.ratingservice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.ratingservice.mapper.RatingMapper;
import com.modsen.taxi.ratingservice.repository.RatingRepository;
import com.modsen.taxi.ratingservice.service.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;
    private final PassengerClient passengerClient;
    private final DriverClient driverClient;

    @Override
    public RatingResponse createRating(RatingRequest ratingRequest) {
        log.info("Creating rating for driver ID: {} and passenger ID: {}", ratingRequest.driverId(), ratingRequest.passengerId());

        if (ratingRepository.existsByDriverIdAndPassengerId(ratingRequest.driverId(), ratingRequest.passengerId())) {
            log.error("Duplicate rating attempt for driver ID: {} and passenger ID: {}", ratingRequest.driverId(), ratingRequest.passengerId());
            throw new DuplicateResourceException("Rating for this driver and passenger already exists.");
        }

        validatePassengerAndDriverExistence(ratingRequest.passengerId(), ratingRequest.driverId());

        Rating rating = ratingMapper.toRating(ratingRequest);
        Rating savedRating = ratingRepository.save(rating);
        log.info("Successfully created rating with ID: {}", savedRating.getId());
        return ratingMapper.toRatingResponse(savedRating);
    }

    @KafkaListener(topics = "rating-topic", groupId = "rating-group")
    public void handleRatingEvent(RatingRequest ratingRequest) {
        log.info("Received rating event for driver ID: {} and passenger ID: {}", ratingRequest.driverId(), ratingRequest.passengerId());

        if (ratingRepository.existsByDriverIdAndPassengerId(ratingRequest.driverId(), ratingRequest.passengerId())) {
            log.error("Duplicate rating event for driver ID: {} and passenger ID: {}", ratingRequest.driverId(), ratingRequest.passengerId());
            throw new DuplicateResourceException("Rating for this driver and passenger already exists.");
        }

        Rating rating = ratingMapper.toRating(ratingRequest);
        ratingRepository.save(rating);
        log.info("Rating event processed successfully for driver ID: {} and passenger ID: {}", ratingRequest.driverId(), ratingRequest.passengerId());
    }

    @Override
    public RatingResponse updateRating(Long id, RatingRequest ratingRequest) {
        log.info("Updating rating with ID: {}", id);
        validatePassengerAndDriverExistence(ratingRequest.passengerId(), ratingRequest.driverId());

        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Rating with ID: {} not found.", id);
                    return new ResourceNotFoundException("Rating with id " + id + " not found.");
                });
        ratingMapper.updateRatingFromRequest(ratingRequest, rating);
        Rating updatedRating = ratingRepository.save(rating);
        log.info("Successfully updated rating with ID: {}", updatedRating.getId());
        return ratingMapper.toRatingResponse(updatedRating);
    }

    @Override
    public RatingResponse getRatingById(Long id) {
        log.info("Fetching rating with ID: {}", id);
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Rating with ID: {} not found.", id);
                    return new ResourceNotFoundException("Rating with id " + id + " not found.");
                });
        return ratingMapper.toRatingResponse(rating);
    }

    @Override
    public Page<RatingResponse> getAllRatings(Pageable pageable, Long driverId, Long passengerId) {
        log.info("Fetching all ratings for driver ID: {} and passenger ID: {}", driverId, passengerId);
        Rating ratingProbe = Rating.builder()
                .driverId(driverId)
                .passengerId(passengerId)
                .build();

        Page<Rating> ratings = ratingRepository.findAll(Example.of(ratingProbe, ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withMatcher("driverId", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("passengerId", ExampleMatcher.GenericPropertyMatchers.exact())), pageable);

        log.info("Fetched {} ratings for driver ID: {} and passenger ID: {}", ratings.getTotalElements(), driverId, passengerId);
        return ratings.map(ratingMapper::toRatingResponse);
    }

    @Override
    public Double getAverageRatingForDriver(Long driverId) {
        log.info("Calculating average rating for driver ID: {}", driverId);
        return ratingRepository.calculateAverageRatingByDriverId(driverId)
                .orElseThrow(() -> {
                    log.error("No ratings found for driver with ID: {}", driverId);
                    return new ResourceNotFoundException("No ratings found for driver with id " + driverId);
                });
    }

    @Override
    public void deleteRating(Long id) {
        log.info("Deleting rating with ID: {}", id);
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Rating with ID: {} not found.", id);
                    return new ResourceNotFoundException("Rating with id " + id + " not found.");
                });
        ratingRepository.delete(rating);
        log.info("Successfully deleted rating with ID: {}", id);
    }

    private void validatePassengerAndDriverExistence(Long passengerId, Long driverId) {
        try {
            passengerClient.getPassengerById(passengerId);
            log.info("Passenger with ID: {} exists.", passengerId);
        } catch (Exception ex) {
            log.error("Passenger with ID: {} not found.", passengerId);
            throw new ResourceNotFoundException("Passenger with id " + passengerId + " not found");
        }

        try {
            driverClient.getDriverById(driverId);
            log.info("Driver with ID: {} exists.", driverId);
        } catch (Exception ex) {
            log.error("Driver with ID: {} not found.", driverId);
            throw new ResourceNotFoundException("Driver with id " + driverId + " not found");
        }
    }
}
