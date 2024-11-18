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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Service
@RequiredArgsConstructor
@Slf4j
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
        log.info("Creating passenger with email: {}", passengerRequest.email());
        return Mono.fromCallable(() -> {
                    boolean exists = passengerRepository.existsByEmail(passengerRequest.email());
                    if (exists) {
                        log.warn("Duplicate passenger with email: {}", passengerRequest.email());
                        throw new DuplicateResourceException("Passenger with email " + passengerRequest.email() + " already exists.");
                    }
                    Passenger passenger = passengerMapper.toPassenger(passengerRequest);
                    passenger.setIsDeleted(false);
                    Passenger savedPassenger = passengerRepository.save(passenger);
                    log.info("Passenger created with ID: {}", savedPassenger.getId());
                    return savedPassenger;
                })
                .subscribeOn(jdbcScheduler)
                .map(passengerMapper::toPassengerResponse);
    }

    @Override
    public Mono<PassengerResponse> updatePassenger(Long id, PassengerRequest passengerRequest, String principalEmail, boolean isAdmin) {
        log.info("Updating passenger with ID: {}", id);
        return Mono.fromCallable(() -> {
                    Passenger passenger = passengerRepository.findByIdAndIsDeletedFalse(id)
                            .orElseThrow(() -> {
                                log.error("Passenger with ID {} not found", id);
                                return new ResourceNotFoundException("Passenger with id " + id + " not found.");
                            });

                    if (!isAdmin && !passenger.getEmail().equals(principalEmail)) {
                        throw new AccessDeniedException("You do not have permission to update this passenger's information.");
                    }
                    passengerMapper.updatePassengerFromRequest(passengerRequest, passenger);
                    Passenger updatedPassenger = passengerRepository.save(passenger);
                    log.info("Passenger updated with ID: {}", updatedPassenger.getId());
                    return updatedPassenger;
                })
                .subscribeOn(jdbcScheduler)
                .map(passengerMapper::toPassengerResponse);
    }


    @Override
    public Mono<Void> deletePassenger(Long id, String principalEmail, boolean isAdmin) {
        log.info("Fetching passenger with ID: {}", id);
        return Mono.fromRunnable(() -> {
                    Passenger passenger = passengerRepository.findByIdAndIsDeletedFalse(id)
                            .orElseThrow(() -> {
                            log.error("Passenger with ID {} not found", id);
                            return new ResourceNotFoundException("Passenger with id " + id + " not found.");
                        }))

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
        log.info("Fetching all passengers with filters - firstName: {}, lastName: {}, email: {}, isActive: {}", firstName, lastName, email, isActive);
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
                    log.info("Fetched {} passengers", passengers.getTotalElements());
                    return passengers.map(passengerMapper::toPassengerResponse);
                })
                .subscribeOn(jdbcScheduler);
    }
}

