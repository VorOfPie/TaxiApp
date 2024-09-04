package com.modsen.taxi.passengerservice.service;

import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PassengerService {
    PassengerResponse createPassenger(PassengerRequest passengerRequest);

    PassengerResponse updatePassenger(Long id, PassengerRequest passengerRequest);

    PassengerResponse getPassengerById(Long id);

    Page<PassengerResponse> getAllPassengers(Pageable pageable, String firstName, String lastName, String email);

    void deletePassenger(Long id);
}
