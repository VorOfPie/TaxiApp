package com.modsen.taxi.passengerservice;

import com.modsen.taxi.passengerservice.domain.Passenger;
import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;
import com.modsen.taxi.passengerservice.error.exception.DuplicateResourceException;
import com.modsen.taxi.passengerservice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.passengerservice.mapper.PassengerMapper;
import com.modsen.taxi.passengerservice.repository.PassengerRepository;
import com.modsen.taxi.passengerservice.service.impl.PassengerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PassengerServiceImplTest {

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private PassengerMapper passengerMapper;

    @Mock
    private Scheduler jdbcScheduler;

    @InjectMocks
    private PassengerServiceImpl passengerService;

    private PassengerRequest passengerRequest;
    private Passenger passenger1;
    private Passenger passenger2;
    private PassengerResponse passengerResponse1;
    private PassengerResponse passengerResponse2;
    private Pageable pageable;

    @BeforeEach
    public void setUp() {
        when(jdbcScheduler.schedule(any())).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return mock(Disposable.class);
        });

        passengerRequest = new PassengerRequest("John", "Doe", "john.doe@example.com", "123123123123");
        passenger1 = new Passenger(1L, "John", "Doe", "john.doe@example.com", "123123123123", false);
        passenger2 = new Passenger(2L, "Jane", "Doe", "jane.doe@example.com", "456456456456", false);
        passengerResponse1 = new PassengerResponse(1L, "John", "Doe", "john.doe@example.com", "123123123123");
        passengerResponse2 = new PassengerResponse(2L, "Jane", "Doe", "jane.doe@example.com", "456456456456");
        pageable = PageRequest.of(0, 2);
    }

    @Test
    void createPassenger_ShouldReturnPassengerResponse_WhenPassengerCreatedSuccessfully() {
        when(passengerRepository.existsByEmail(anyString())).thenReturn(false);
        when(passengerMapper.toPassenger(passengerRequest)).thenReturn(passenger1);
        when(passengerRepository.save(passenger1)).thenReturn(passenger1);
        when(passengerMapper.toPassengerResponse(passenger1)).thenReturn(passengerResponse1);

        Mono<PassengerResponse> result = passengerService.createPassenger(passengerRequest);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.email().equals("john.doe@example.com"))
                .verifyComplete();

        verify(passengerRepository).existsByEmail(anyString());
        verify(passengerRepository).save(passenger1);
    }

    @Test
    void createPassenger_ShouldThrowDuplicateResourceException_WhenPassengerAlreadyExists() {
        when(passengerRepository.existsByEmail(anyString())).thenReturn(true);

        Mono<PassengerResponse> result = passengerService.createPassenger(passengerRequest);

        StepVerifier.create(result)
                .expectError(DuplicateResourceException.class)
                .verify();

        verify(passengerRepository).existsByEmail(anyString());
        verify(passengerRepository, never()).save(any(Passenger.class));
    }

    @Test
    void getPassengerById_ShouldReturnPassengerResponse_WhenPassengerExists() {
        when(passengerRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(passenger1));
        when(passengerMapper.toPassengerResponse(passenger1)).thenReturn(passengerResponse1);

        Mono<PassengerResponse> result = passengerService.getPassengerById(1L);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.email().equals("john.doe@example.com"))
                .verifyComplete();

        verify(passengerRepository).findByIdAndIsDeletedFalse(anyLong());
    }

    @Test
    void getPassengerById_ShouldThrowResourceNotFoundException_WhenPassengerNotFound() {
        when(passengerRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.empty());

        Mono<PassengerResponse> result = passengerService.getPassengerById(1L);

        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(passengerRepository).findByIdAndIsDeletedFalse(anyLong());
    }

    @Test
    void deletePassenger_ShouldMarkPassengerAsDeleted_WhenPassengerExists() {
        when(passengerRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(passenger1));

        Mono<Void> result = passengerService.deletePassenger(1L);

        StepVerifier.create(result)
                .verifyComplete();

        verify(passengerRepository).findByIdAndIsDeletedFalse(anyLong());
        assert passenger1.getIsDeleted();
        verify(passengerRepository).save(passenger1);
    }

    @Test
    void deletePassenger_ShouldThrowResourceNotFoundException_WhenPassengerDoesNotExist() {
        when(passengerRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.empty());

        Mono<Void> result = passengerService.deletePassenger(1L);

        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(passengerRepository, never()).save(any(Passenger.class));
    }

    @Test
    void getAllPassengers_ShouldReturnPagedPassengers_WhenPassengersExist() {
        Page<Passenger> passengerPage = new PageImpl<>(List.of(passenger1, passenger2), pageable, 2);
        when(passengerRepository.findAll(any(Example.class), any(Pageable.class))).thenReturn(passengerPage);
        when(passengerMapper.toPassengerResponse(passenger1)).thenReturn(passengerResponse1);
        when(passengerMapper.toPassengerResponse(passenger2)).thenReturn(passengerResponse2);

        Mono<Page<PassengerResponse>> result = passengerService.getAllPassengers(pageable, null, null, null, true);

        StepVerifier.create(result)
                .expectNextMatches(page -> {
                    assertEquals(2, page.getSize());
                    assertEquals(2, page.getTotalElements());
                    List<PassengerResponse> responses = page.getContent();
                    return responses.size() == 2 &&
                            responses.get(0).equals(passengerResponse1) &&
                            responses.get(1).equals(passengerResponse2);
                })
                .verifyComplete();

        verify(passengerRepository).findAll(any(Example.class), eq(pageable));
    }

    @Test
    void getAllPassengers_ShouldReturnFilteredPassengers_WhenFiltersAreApplied() {
        Page<Passenger> passengerPage = new PageImpl<>(List.of(passenger1), pageable, 1);
        when(passengerRepository.findAll(any(Example.class), eq(pageable))).thenReturn(passengerPage);
        when(passengerMapper.toPassengerResponse(passenger1)).thenReturn(passengerResponse1);

        Mono<Page<PassengerResponse>> result = passengerService.getAllPassengers(pageable, "John", "", "", true);

        StepVerifier.create(result)
                .expectNextMatches(page -> {
                    List<PassengerResponse> passengers = page.getContent();
                    return passengers.size() == 1 &&
                            passengers.get(0).firstName().equals("John") &&
                            passengers.get(0).lastName().equals("Doe");
                })
                .verifyComplete();

        verify(passengerRepository).findAll(any(Example.class), eq(pageable));
        verify(passengerMapper).toPassengerResponse(passenger1);
    }
}
