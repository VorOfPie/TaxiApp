package com.modsen.taxi.passengerservice.service;

import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface PassengerService {
    Mono<PassengerResponse> createPassenger(PassengerRequest passengerRequest);

    Mono<PassengerResponse> updatePassenger(Long id, PassengerRequest passengerRequest);

    Mono<PassengerResponse> getPassengerById(Long id);

    Mono<Page<PassengerResponse>> getAllPassengers(Pageable pageable, String firstName, String lastName, String email, boolean isActive);

    Mono<Void> deletePassenger(Long id);
}
