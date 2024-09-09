package com.modsen.taxi.tripservice.service;

import com.modsen.taxi.tripservice.dto.TripRequest;
import com.modsen.taxi.tripservice.dto.TripResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TripService {
    TripResponse createTrip(TripRequest tripRequest);

    TripResponse updateTrip(Long id, TripRequest tripRequest);

    TripResponse getTripById(Long id);

    Page<TripResponse> getAllTrips(Pageable pageable, Long driverId, Long passengerId, String originAddress, String destinationAddress, String status);

    TripResponse updateTripStatus(Long id, String status);

    void deleteTrip(Long id);
}
