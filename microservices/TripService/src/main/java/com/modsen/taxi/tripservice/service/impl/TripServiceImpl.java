package com.modsen.taxi.tripservice.service.impl;

import com.modsen.taxi.tripservice.config.DriverClient;
import com.modsen.taxi.tripservice.config.PassengerClient;
import com.modsen.taxi.tripservice.domain.Trip;
import com.modsen.taxi.tripservice.domain.TripStatus;
import com.modsen.taxi.tripservice.dto.request.RatingRequest;
import com.modsen.taxi.tripservice.dto.request.ScoreRequest;
import com.modsen.taxi.tripservice.dto.request.TripRequest;
import com.modsen.taxi.tripservice.dto.response.DriverResponse;
import com.modsen.taxi.tripservice.dto.response.PassengerResponse;
import com.modsen.taxi.tripservice.dto.response.TripResponse;
import com.modsen.taxi.tripservice.error.exception.InvalidRequestException;
import com.modsen.taxi.tripservice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.tripservice.mapper.TripMapper;
import com.modsen.taxi.tripservice.repository.TripRepository;
import com.modsen.taxi.tripservice.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final TripMapper tripMapper;
    private final PassengerClient passengerClient;
    private final DriverClient driverClient;
    private final KafkaTemplate<String, RatingRequest> kafkaTemplate;

    @Override
    @Transactional
    public TripResponse createTrip(TripRequest tripRequest) {
        log.info("Creating trip for passenger ID: {} and driver ID: {}", tripRequest.passengerId(), tripRequest.driverId());
        validatePassengerAndDriverExistence(tripRequest.passengerId(), tripRequest.driverId());

        Trip trip = tripMapper.toEntity(tripRequest);
        trip.setStatus(TripStatus.CREATED);
        Trip savedTrip = tripRepository.save(trip);
        log.info("Successfully created trip with ID: {}", savedTrip.getId());

        return tripMapper.toDTO(savedTrip);
    }

    @Override
    @Transactional
    public TripResponse updateTrip(Long id, TripRequest tripRequest) {
        log.info("Updating trip with ID: {}", id);
        validatePassengerAndDriverExistence(tripRequest.passengerId(), tripRequest.driverId());

        Trip existingTrip = tripRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Trip with ID: {} not found", id);
                    return new ResourceNotFoundException("Trip with id " + id + " not found");
                });

        tripMapper.updateTripFromRequest(tripRequest, existingTrip);
        Trip updatedTrip = tripRepository.save(existingTrip);
        log.info("Successfully updated trip with ID: {}", updatedTrip.getId());
        return tripMapper.toDTO(updatedTrip);
    }

    @Override
    public TripResponse getTripById(Long id) {
        log.info("Fetching trip with ID: {}", id);
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Trip with ID: {} not found", id);
                    return new ResourceNotFoundException("Trip with id " + id + " not found");
                });
        return tripMapper.toDTO(trip);
    }

    @Override
    public Page<TripResponse> getAllTrips(Pageable pageable, Long driverId, Long passengerId, String originAddress, String destinationAddress, String status) {
        log.info("Fetching all trips with filters - Driver ID: {}, Passenger ID: {}, Origin: {}, Destination: {}, Status: {}",
                driverId, passengerId, originAddress, destinationAddress, status);

        Trip probe = Trip.builder()
                .driverId(driverId)
                .passengerId(passengerId)
                .originAddress(originAddress)
                .destinationAddress(destinationAddress)
                .status(status != null ? TripStatus.valueOf(status.toUpperCase()) : null)
                .build();

        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreNullValues()
                .withMatcher("originAddress", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("destinationAddress", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("status", ExampleMatcher.GenericPropertyMatchers.exact());

        Example<Trip> example = Example.of(probe, matcher);

        Page<Trip> trips = tripRepository.findAll(example, pageable);
        log.info("Fetched {} trips", trips.getTotalElements());

        return trips.map(tripMapper::toDTO);
    }

    @Override
    @Transactional
    public TripResponse updateTripStatus(Long id, String status) {
        log.info("Updating status for trip ID: {} to {}", id, status);
        Trip existingTrip = tripRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Trip with ID: {} not found", id);
                    return new ResourceNotFoundException("Trip with id " + id + " not found");
                });

        existingTrip.setStatus(TripStatus.valueOf(status.toUpperCase()));
        Trip updatedTrip = tripRepository.save(existingTrip);
        log.info("Successfully updated status for trip ID: {}", updatedTrip.getId());
        return tripMapper.toDTO(updatedTrip);
    }

    @Override
    public void deleteTrip(Long id) {
        log.info("Deleting trip with ID: {}", id);
        Trip existingTrip = tripRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Trip with ID: {} not found", id);
                    return new ResourceNotFoundException("Trip not found with id: " + id);
                });

        tripRepository.delete(existingTrip);
        log.info("Successfully deleted trip with ID: {}", id);
    }

    @Override
    @Transactional
    public void closeAndRateTrip(Long id, ScoreRequest scoreRequest) {
        log.info("Closing trip with ID: {} and processing rating", id);
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Trip with ID: {} not found", id);
                    return new ResourceNotFoundException("Trip with id " + id + " not found");
                });

        try {
            RatingRequest ratingRequest = RatingRequest.builder()
                    .driverId(trip.getDriverId())
                    .passengerId(trip.getPassengerId())
                    .score(scoreRequest.score())
                    .comment(scoreRequest.comment())
                    .build();

            trip.setStatus(TripStatus.COMPLETED);
            tripRepository.save(trip);

            Message<RatingRequest> message = MessageBuilder.
                    withPayload(ratingRequest)
                    .setHeader(KafkaHeaders.TOPIC, "rating-topic")
                    .build();
            kafkaTemplate.send(message);
            log.info("Successfully sent rating event for trip ID: {}", id);

        } catch (Exception ex) {
            log.error("Failed to send rating event via Kafka for trip ID: {}. Error: {}", id, ex.getMessage());
            throw new InvalidRequestException("Failed to send rating event via Kafka");
        }
    }

    private void validatePassengerAndDriverExistence(Long passengerId, Long driverId) {
        log.info("Validating existence of passenger ID: {} and driver ID: {}", passengerId, driverId);
        try {
            passengerClient.getPassengerById(passengerId);
            log.info("Passenger with ID: {} exists", passengerId);
        } catch (Exception ex) {
            log.error("Passenger with ID: {} not found", passengerId);
            throw new ResourceNotFoundException("Passenger with id " + passengerId + " not found");
        }

        try {
            driverClient.getDriverById(driverId);
            log.info("Driver with ID: {} exists", driverId);
        } catch (Exception ex) {
            log.error("Driver with ID: {} not found", driverId);
            throw new ResourceNotFoundException("Driver with id " + driverId + " not found");
        }
    }
}
