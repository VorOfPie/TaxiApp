package com.modsen.taxi.ratingservice.service.impl;

import com.modsen.taxi.ratingservice.domain.Rating;
import com.modsen.taxi.ratingservice.dto.RatingRequest;
import com.modsen.taxi.ratingservice.dto.response.DriverResponse;
import com.modsen.taxi.ratingservice.dto.response.PassengerResponse;
import com.modsen.taxi.ratingservice.dto.response.RatingResponse;
import com.modsen.taxi.ratingservice.error.exception.DuplicateResourceException;
import com.modsen.taxi.ratingservice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.ratingservice.mapper.RatingMapper;
import com.modsen.taxi.ratingservice.repository.RatingRepository;
import com.modsen.taxi.ratingservice.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;
    @Value("${ratingservice.urls.passenger}")
    private String passengerServiceUrl;

    @Value("${ratingservice.urls.driver}")
    private String driverServiceUrl;

    private final RestTemplate restTemplate;

    @Override
    public RatingResponse createRating(RatingRequest ratingRequest) {
        PassengerResponse passenger = getPassengerById(ratingRequest.passengerId());
        DriverResponse driver = getDriverById(ratingRequest.driverId());
        boolean exists = ratingRepository.existsByDriverIdAndPassengerId(ratingRequest.driverId(), ratingRequest.passengerId());
        if (exists) {
            throw new DuplicateResourceException("Rating for this driver and passenger already exists.");
        }
        Rating rating = ratingMapper.toRating(ratingRequest);
        Rating savedRating = ratingRepository.save(rating);
        return ratingMapper.toRatingResponse(savedRating);
    }

    @Override
    public RatingResponse updateRating(Long id, RatingRequest ratingRequest) {
        PassengerResponse passenger = getPassengerById(ratingRequest.passengerId());
        DriverResponse driver = getDriverById(ratingRequest.driverId());
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
        ratingRepository.delete(rating);
    }
    private PassengerResponse getPassengerById(Long passengerId) {
        try {
            ResponseEntity<PassengerResponse> response = restTemplate.exchange(
                    passengerServiceUrl + passengerId,
                    HttpMethod.GET,
                    null,
                    PassengerResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResourceNotFoundException("Passenger not found with id: " + passengerId);
            } else {
                throw e;
            }
        }
    }

    private DriverResponse getDriverById(Long driverId) {
        try {
            ResponseEntity<DriverResponse> response = restTemplate.exchange(
                    driverServiceUrl + driverId,
                    HttpMethod.GET,
                    null,
                    DriverResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResourceNotFoundException("Driver not found with id: " + driverId);
            } else {
                throw e;
            }
        }
    }
}
