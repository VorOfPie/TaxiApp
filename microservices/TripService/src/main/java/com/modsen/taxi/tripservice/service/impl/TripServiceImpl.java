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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final TripMapper tripMapper;
    private final PassengerClient passengerClient;
    private final DriverClient driverClient;

    private final KafkaTemplate<String, RatingRequest> kafkaTemplate;

    @Override
    @Transactional
    public TripResponse createTrip(TripRequest tripRequest) {
        validatePassengerAndDriverExistence(tripRequest.passengerId(), tripRequest.driverId());

        Trip trip = tripMapper.toEntity(tripRequest);
        trip.setStatus(TripStatus.CREATED);
        Trip savedTrip = tripRepository.save(trip);

        return tripMapper.toDTO(savedTrip);
    }

    @Override
    @Transactional
    public TripResponse updateTrip(Long id, TripRequest tripRequest) {
        validatePassengerAndDriverExistence(tripRequest.passengerId(), tripRequest.driverId());

        Trip existingTrip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + id));
        tripMapper.updateTripFromRequest(tripRequest, existingTrip);
        Trip updatedTrip = tripRepository.save(existingTrip);
        return tripMapper.toDTO(updatedTrip);
    }

    @Override
    public TripResponse getTripById(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + id));
        return tripMapper.toDTO(trip);
    }

    @Override
    public Page<TripResponse> getAllTrips(Pageable pageable, Long driverId, Long passengerId, String originAddress, String destinationAddress, String status) {
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

        return trips.map(tripMapper::toDTO);
    }

    @Override
    @Transactional
    public TripResponse updateTripStatus(Long id, String status) {
        Trip existingTrip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + id));

        existingTrip.setStatus(TripStatus.valueOf(status.toUpperCase()));
        Trip updatedTrip = tripRepository.save(existingTrip);
        return tripMapper.toDTO(updatedTrip);
    }

    @Override
    public void deleteTrip(Long id) {
        Trip existingTrip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + id));

        tripRepository.delete(existingTrip);
    }

    @Override
    @Transactional
    public void closeAndRateTrip(Long id, ScoreRequest scoreRequest) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + id));

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

        } catch (Exception ex) {
            throw new InvalidRequestException("Failed to send rating event via Kafka");
        }
    }

    private void validatePassengerAndDriverExistence(Long passengerId, Long driverId) {
        try {
            PassengerResponse passenger = passengerClient.getPassengerById(passengerId);
        } catch (Exception ex) {
            throw new ResourceNotFoundException("Passenger not found with id: " + passengerId);
        }

        try {
            DriverResponse driver = driverClient.getDriverById(driverId);
        } catch (Exception ex) {
            throw new ResourceNotFoundException("Driver not found with id: " + driverId);
        }
    }

}
