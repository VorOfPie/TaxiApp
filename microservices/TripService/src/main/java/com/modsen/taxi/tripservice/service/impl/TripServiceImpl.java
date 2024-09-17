package com.modsen.taxi.tripservice.service.impl;

import com.modsen.taxi.tripservice.domain.Trip;
import com.modsen.taxi.tripservice.domain.TripStatus;
import com.modsen.taxi.tripservice.dto.TripRequest;
import com.modsen.taxi.tripservice.dto.response.DriverResponse;
import com.modsen.taxi.tripservice.dto.response.PassengerResponse;
import com.modsen.taxi.tripservice.dto.response.TripResponse;
import com.modsen.taxi.tripservice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.tripservice.mapper.TripMapper;
import com.modsen.taxi.tripservice.repository.TripRepository;
import com.modsen.taxi.tripservice.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final TripMapper tripMapper;
    private final RestTemplate restTemplate;

    @Value("${tripservice.urls.passenger}")
    private String passengerServiceUrl;

    @Value("${tripservice.urls.driver}")
    private String driverServiceUrl;


    @Override
    @Transactional
    public TripResponse createTrip(TripRequest tripRequest) {
        PassengerResponse passenger = getPassengerById(tripRequest.passengerId());
        DriverResponse driver = getDriverById(tripRequest.driverId());
        Trip trip = tripMapper.toEntity(tripRequest);
        trip.setStatus(TripStatus.CREATED);
        Trip savedTrip = tripRepository.save(trip);

        return tripMapper.toDTO(savedTrip);
    }



    @Override
    @Transactional
    public TripResponse updateTrip(Long id, TripRequest tripRequest) {
        PassengerResponse passenger = getPassengerById(tripRequest.passengerId());
        DriverResponse driver = getDriverById(tripRequest.driverId());
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
