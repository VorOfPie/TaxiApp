package com.modsen.taxi.passengerservice.service;

import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;

import java.util.List;

public interface PassengerService {
    PassengerResponse createPassenger(PassengerRequest passengerRequest);

    PassengerResponse updatePassenger(Long id, PassengerRequest passengerRequest);

    PassengerResponse getPassengerById(Long id);

    List<PassengerResponse> getAllPassengers();

    void deletePassenger(Long id);
}
