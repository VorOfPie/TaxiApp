package com.modsen.taxi.ratingservice.service.impl;

import com.modsen.taxi.ratingservice.domain.Rating;
import com.modsen.taxi.ratingservice.dto.RatingRequest;
import com.modsen.taxi.ratingservice.dto.response.RatingResponse;
import com.modsen.taxi.ratingservice.error.exception.DuplicateResourceException;
import com.modsen.taxi.ratingservice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.ratingservice.mapper.RatingMapper;
import com.modsen.taxi.ratingservice.repository.RatingRepository;
import com.modsen.taxi.ratingservice.service.RatingService;
import com.modsen.taxi.ratingservice.util.PassengerDriverValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;
    private final PassengerDriverValidator passengerDriverValidator;

    @Override
    @Transactional
    public RatingResponse createRating(RatingRequest ratingRequest) {
        if (ratingRepository.existsByDriverIdAndPassengerId(ratingRequest.driverId(), ratingRequest.passengerId())) {
            throw new DuplicateResourceException("Rating for this driver and passenger already exists.");
        }
        passengerDriverValidator.validatePassengerAndDriverExistence(ratingRequest.passengerId(), ratingRequest.driverId());
        Rating rating = ratingMapper.toRating(ratingRequest);
        Rating savedRating = ratingRepository.save(rating);
        return ratingMapper.toRatingResponse(savedRating);
    }

    @KafkaListener(topics = "rating-topic", groupId = "rating-group")
    public void handleRatingEvent(RatingRequest ratingRequest) {
        boolean exists = ratingRepository.existsByDriverIdAndPassengerId(ratingRequest.driverId(), ratingRequest.passengerId());
        if (exists) {
            throw new DuplicateResourceException("Rating for this driver and passenger already exists.");
        }
        Rating rating = ratingMapper.toRating(ratingRequest);
        ratingRepository.save(rating);
    }

    @Override
    @Transactional
    public RatingResponse updateRating(Long id, RatingRequest ratingRequest) {
        passengerDriverValidator.validatePassengerAndDriverAccess(ratingRequest.passengerId(), ratingRequest.driverId());

        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rating with id " + id + " not found."));
        ratingMapper.updateRatingFromRequest(ratingRequest, rating);
        Rating updatedRating = ratingRepository.save(rating);
        return ratingMapper.toRatingResponse(updatedRating);
    }

    @Override
    public RatingResponse getRatingById(Long id) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rating with id " + id + " not found."));
        passengerDriverValidator.validatePassengerAndDriverAccess(rating.getPassengerId(), rating.getDriverId());
        return ratingMapper.toRatingResponse(rating);
    }

    @Override
    public Page<RatingResponse> getAllRatings(Pageable pageable, Long driverId, Long passengerId) {
        Rating ratingProbe = Rating.builder()
                .driverId(driverId)
                .passengerId(passengerId)
                .build();

        Page<Rating> ratings = ratingRepository.findAll(Example.of(ratingProbe, ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withMatcher("driverId", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("passengerId", ExampleMatcher.GenericPropertyMatchers.exact())), pageable);

        return ratings.map(ratingMapper::toRatingResponse);
    }

    @Override
    public Double getAverageRatingForDriver(Long driverId) {
        return ratingRepository.calculateAverageRatingByDriverId(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("No ratings found for driver with id " + driverId));
    }

    @Override
    public void deleteRating(Long id) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rating with id " + id + " not found."));
        passengerDriverValidator.validatePassengerAndDriverAccess(rating.getPassengerId(), rating.getDriverId());
        ratingRepository.delete(rating);
    }
}
