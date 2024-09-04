package com.modsen.taxi.passengerservice.service.impl;

import com.modsen.taxi.passengerservice.domain.Passenger;
import com.modsen.taxi.passengerservice.mapper.PassengerMapper;
import com.modsen.taxi.passengerservice.repository.PassengerRepository;
import com.modsen.taxi.passengerservice.service.PassengerService;
import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;
import com.modsen.taxi.passengerservice.error.exception.DuplicateResourceException;
import com.modsen.taxi.passengerservice.error.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {

    private final PassengerRepository passengerRepository;
    private final PassengerMapper passengerMapper;

    @Override
    public PassengerResponse createPassenger(PassengerRequest passengerRequest) {
        boolean exists = passengerRepository.existsByEmail(passengerRequest.email());
        if (exists) {
            throw new DuplicateResourceException("Passenger with email " + passengerRequest.email() + " already exists.");
        }
        Passenger passenger = passengerMapper.toPassenger(passengerRequest);
        passenger.setIsDeleted(false);
        Passenger savedPassenger = passengerRepository.save(passenger);
        return passengerMapper.toPassengerResponse(savedPassenger);
    }

    @Override
    public PassengerResponse updatePassenger(Long id, PassengerRequest passengerRequest) {
        Passenger passenger = passengerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger with id " + id + " not found."));
        passengerMapper.updatePassengerFromRequest(passengerRequest, passenger);
        Passenger savedPassenger = passengerRepository.save(passenger);
        return passengerMapper.toPassengerResponse(savedPassenger);
    }

    @Override
    public PassengerResponse getPassengerById(Long id) {
        Passenger passenger = passengerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger with id " + id + " not found."));
        return passengerMapper.toPassengerResponse(passenger);
    }


    @Override
    public Page<PassengerResponse> getAllPassengers(Pageable pageable, String firstName, String lastName, String email) {
        Passenger passengerProbe = Passenger.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .isDeleted(false)
                .build();

        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreNullValues()
                .withMatcher("firstName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("lastName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("email", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

        Example<Passenger> example = Example.of(passengerProbe, matcher);

        Page<Passenger> passengers = passengerRepository.findAll(example, pageable);
        return passengers.map(passengerMapper::toPassengerResponse);
    }
    @Override
    public void deletePassenger(Long id) {
        Passenger passenger = passengerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger with id " + id + " not found."));
        passenger.setIsDeleted(true);
        passengerRepository.save(passenger);
    }
}
