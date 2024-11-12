package com.modsen.taxi.passengerservice.service.impl;

import com.modsen.taxi.passengerservice.domain.Passenger;
import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;
import com.modsen.taxi.passengerservice.error.exception.AccessDeniedException;
import com.modsen.taxi.passengerservice.error.exception.DuplicateResourceException;
import com.modsen.taxi.passengerservice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.passengerservice.mapper.PassengerMapper;
import com.modsen.taxi.passengerservice.repository.PassengerRepository;
import com.modsen.taxi.passengerservice.service.PassengerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Service
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {

    private final PassengerRepository passengerRepository;
    private final PassengerMapper passengerMapper;
    private final Scheduler jdbcScheduler;

    @Override
    public Mono<PassengerResponse> getPassengerById(Long id, String principalEmail, boolean isAdmin) {
        return Mono.fromCallable(() -> {
                    Passenger passenger = passengerRepository.findByIdAndIsDeletedFalse(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Passenger with id " + id + " not found."));

                    if (!isAdmin && !passenger.getEmail().equals(principalEmail)) {
                        throw new AccessDeniedException("You do not have permission to access this passenger's information.");
                    }

                    return passenger;
                })
                .subscribeOn(jdbcScheduler)
                .map(passengerMapper::toPassengerResponse);
    }

    @Override
    public Mono<PassengerResponse> createPassenger(PassengerRequest passengerRequest) {
        return Mono.fromCallable(() -> {
                    boolean exists = passengerRepository.existsByEmail(passengerRequest.email());
                    if (exists) {
                        throw new DuplicateResourceException("Passenger with email " + passengerRequest.email() + " already exists.");
                    }
                    Passenger passenger = passengerMapper.toPassenger(passengerRequest);
                    passenger.setIsDeleted(false);
                    return passengerRepository.save(passenger);
                })
                .subscribeOn(jdbcScheduler)
                .map(passengerMapper::toPassengerResponse);
    }

    @Override
    public Mono<PassengerResponse> updatePassenger(Long id, PassengerRequest passengerRequest, String principalEmail, boolean isAdmin) {
        return Mono.fromCallable(() -> {
                    Passenger passenger = passengerRepository.findByIdAndIsDeletedFalse(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Passenger with id " + id + " not found."));

                    if (!isAdmin && !passenger.getEmail().equals(principalEmail)) {
                        throw new AccessDeniedException("You do not have permission to update this passenger's information.");
                    }
                    passengerMapper.updatePassengerFromRequest(passengerRequest, passenger);
                    return passengerRepository.save(passenger);
                })
                .subscribeOn(jdbcScheduler)
                .map(passengerMapper::toPassengerResponse);
    }


    @Override
    public Mono<Void> deletePassenger(Long id, String principalEmail, boolean isAdmin) {
        return Mono.fromRunnable(() -> {
                    Passenger passenger = passengerRepository.findByIdAndIsDeletedFalse(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Passenger with id " + id + " not found."));

                    if (!isAdmin && !passenger.getEmail().equals(principalEmail)) {
                        throw new AccessDeniedException("You do not have permission to delete this passenger.");
                    }

                    passenger.setIsDeleted(true);
                    passengerRepository.save(passenger);
                })
                .subscribeOn(jdbcScheduler)
                .then();
    }

    @Override
    public Mono<Page<PassengerResponse>> getAllPassengers(Pageable pageable, String firstName, String lastName, String email, boolean isActive) {
        return Mono.fromCallable(() -> {
                    Passenger passengerProbe = Passenger.builder()
                            .firstName(firstName)
                            .lastName(lastName)
                            .email(email)
                            .isDeleted(!isActive)
                            .build();

                    ExampleMatcher matcher = ExampleMatcher.matchingAll()
                            .withIgnoreNullValues()
                            .withMatcher("firstName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                            .withMatcher("lastName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                            .withMatcher("email", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

                    Example<Passenger> example = Example.of(passengerProbe, matcher);

                    Page<Passenger> passengers = passengerRepository.findAll(example, pageable);
                    return passengers.map(passengerMapper::toPassengerResponse);
                })
                .subscribeOn(jdbcScheduler);
    }
}

