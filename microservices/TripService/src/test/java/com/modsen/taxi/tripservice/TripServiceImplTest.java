package com.modsen.taxi.tripservice;

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
import com.modsen.taxi.tripservice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.tripservice.mapper.TripMapper;
import com.modsen.taxi.tripservice.repository.TripRepository;
import com.modsen.taxi.tripservice.service.impl.TripServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.GenericMessage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TripServiceImplTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripMapper tripMapper;

    @Mock
    private PassengerClient passengerClient;

    @Mock
    private DriverClient driverClient;

    @Mock
    private KafkaTemplate<String, RatingRequest> kafkaTemplate;

    @InjectMocks
    private TripServiceImpl tripService;

    private TripRequest tripRequest;
    private Trip trip;
    private TripResponse tripResponse;
    private PassengerResponse passengerResponse;
    private DriverResponse driverResponse;

    private Trip trip1;
    private Trip trip2;
    private TripResponse tripResponse1;
    private TripResponse tripResponse2;
    private Pageable pageable;

    @BeforeEach
    public void setUp() {
        tripRequest = new TripRequest(
                1L,
                1L,
                "Origin",
                "Destination",
                TripStatus.CREATED.name(),
                LocalDateTime.now(),
                BigDecimal.valueOf(100.0)
        );

        trip = new Trip(1L, 1L, 1L, "Origin", "Destination", TripStatus.CREATED, LocalDateTime.now(), BigDecimal.valueOf(100.0));
        tripResponse = new TripResponse(1L, 1L, 1L, "Origin", "Destination", TripStatus.CREATED.name(), LocalDateTime.now(), BigDecimal.valueOf(100.0));
        passengerResponse = new PassengerResponse(1L, "Passenger Name", "Last Name", "passenger@example.com", "1234567890");
        driverResponse = new DriverResponse(1L, "Driver Name", "Last Name", "driver@example.com", "Male", null);

        trip1 = new Trip(1L, 1L, 1L, "Origin 1", "Destination 1", TripStatus.CREATED, LocalDateTime.now(), BigDecimal.valueOf(100.0));
        trip2 = new Trip(2L, 2L, 2L, "Origin 2", "Destination 2", TripStatus.CREATED, LocalDateTime.now(), BigDecimal.valueOf(200.0));

        tripResponse1 = new TripResponse(1L, 1L, 1L, "Origin 1", "Destination 1", TripStatus.CREATED.name(), LocalDateTime.now(), BigDecimal.valueOf(100.0));
        tripResponse2 = new TripResponse(2L, 2L, 2L, "Origin 2", "Destination 2", TripStatus.CREATED.name(), LocalDateTime.now(), BigDecimal.valueOf(200.0));

        pageable = PageRequest.of(0, 2);
    }

    @Test
    void createTrip_ShouldReturnTripResponse_WhenTripCreatedSuccessfully() {
        when(passengerClient.getPassengerById(anyLong())).thenReturn(passengerResponse);
        when(driverClient.getDriverById(anyLong())).thenReturn(driverResponse);
        when(tripMapper.toEntity(tripRequest)).thenReturn(trip);
        when(tripRepository.save(trip)).thenReturn(trip);
        when(tripMapper.toDTO(trip)).thenReturn(tripResponse);

        TripResponse result = tripService.createTrip(tripRequest);

        assertEquals(tripResponse, result);
        verify(tripRepository).save(trip);
    }

    @Test
    void updateTrip_ShouldReturnUpdatedTripResponse_WhenTripExists() {
        when(tripRepository.findById(anyLong())).thenReturn(Optional.of(trip));
        when(passengerClient.getPassengerById(anyLong())).thenReturn(passengerResponse);
        when(driverClient.getDriverById(anyLong())).thenReturn(driverResponse);

        doAnswer(invocation -> {
            Trip t = invocation.getArgument(1);
            t.setOriginAddress(tripRequest.originAddress());
            t.setDestinationAddress(tripRequest.destinationAddress());
            return null;
        }).when(tripMapper).updateTripFromRequest(any(), any());

        when(tripRepository.save(any(Trip.class))).thenReturn(trip);
        when(tripMapper.toDTO(trip)).thenReturn(tripResponse);

        TripResponse result = tripService.updateTrip(1L, tripRequest);

        assertEquals(tripResponse, result);
        verify(tripRepository).save(trip);
        verify(tripMapper).updateTripFromRequest(any(), any());
    }

    @Test
    void getTripById_ShouldReturnTripResponse_WhenTripExists() {
        when(tripRepository.findById(anyLong())).thenReturn(Optional.of(trip));
        when(tripMapper.toDTO(trip)).thenReturn(tripResponse);

        TripResponse result = tripService.getTripById(1L);

        assertEquals(tripResponse, result);
        verify(tripRepository).findById(anyLong());
    }

    @Test
    void getTripById_ShouldThrowResourceNotFoundException_WhenTripNotFound() {
        when(tripRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> tripService.getTripById(1L));

        assertEquals("Trip not found with id: 1", exception.getMessage());
        verify(tripRepository).findById(anyLong());
    }

    @Test
    void updateTripStatus_ShouldReturnUpdatedTripResponse_WhenTripExists() {
        when(tripRepository.findById(anyLong())).thenReturn(Optional.of(trip));
        when(tripRepository.save(trip)).thenReturn(trip);
        when(tripMapper.toDTO(trip)).thenReturn(tripResponse);

        TripResponse result = tripService.updateTripStatus(1L, TripStatus.COMPLETED.name());

        assertEquals(tripResponse, result);
        assertEquals(TripStatus.COMPLETED, trip.getStatus());
        verify(tripRepository).save(trip);
    }

    @Test
    void deleteTrip_ShouldDeleteTrip_WhenTripExists() {
        when(tripRepository.findById(anyLong())).thenReturn(Optional.of(trip));

        tripService.deleteTrip(1L);

        verify(tripRepository).delete(trip);
    }

    @Test
    void deleteTrip_ShouldThrowResourceNotFoundException_WhenTripNotFound() {
        when(tripRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> tripService.deleteTrip(1L));

        assertEquals("Trip not found with id: 1", exception.getMessage());
        verify(tripRepository, never()).delete(any(Trip.class));
    }

    @Test
    void closeAndRateTrip_ShouldSendRatingRequest_WhenTripExists() {
        ScoreRequest scoreRequest = new ScoreRequest(5.0, "Great trip!");
        when(tripRepository.findById(anyLong())).thenReturn(Optional.of(trip));
        when(tripRepository.save(trip)).thenReturn(trip);

        tripService.closeAndRateTrip(1L, scoreRequest);

        ArgumentCaptor<GenericMessage<RatingRequest>> messageCaptor = ArgumentCaptor.forClass(GenericMessage.class);

        verify(kafkaTemplate).send(messageCaptor.capture());

        GenericMessage<RatingRequest> capturedMessage = messageCaptor.getValue();
        RatingRequest capturedRatingRequest = capturedMessage.getPayload();

        assertEquals("rating-topic", capturedMessage.getHeaders().get(KafkaHeaders.TOPIC));
        assertEquals(1L, capturedRatingRequest.driverId());
        assertEquals(1L, capturedRatingRequest.passengerId());
        assertEquals(5.0, capturedRatingRequest.score());
        assertEquals("Great trip!", capturedRatingRequest.comment());

        assertEquals(TripStatus.COMPLETED, trip.getStatus());
        verify(tripRepository).save(trip);
    }

    @Test
    void getAllTrips_ShouldReturnPagedTrips_WhenTripsExist() {
        Page<Trip> tripPage = new PageImpl<>(List.of(trip1, trip2), pageable, 2);
        when(tripRepository.findAll(any(Example.class), any(Pageable.class))).thenReturn(tripPage);
        when(tripMapper.toDTO(trip1)).thenReturn(tripResponse1);
        when(tripMapper.toDTO(trip2)).thenReturn(tripResponse2);

        Page<TripResponse> result = tripService.getAllTrips(pageable, null, null, null, null, null);

        assertEquals(2, result.getSize());
        assertEquals(2, result.getTotalElements());
        List<TripResponse> responses = result.getContent();
        assertEquals(2, responses.size());
        assertEquals(tripResponse1, responses.get(0));
        assertEquals(tripResponse2, responses.get(1));

        verify(tripRepository).findAll(any(Example.class), eq(pageable));
    }

    @Test
    void getAllTrips_ShouldReturnFilteredTrips_WhenFiltersAreApplied() {
        Page<Trip> tripPage = new PageImpl<>(List.of(trip1), pageable, 1);
        when(tripRepository.findAll(any(Example.class), eq(pageable))).thenReturn(tripPage);
        when(tripMapper.toDTO(trip1)).thenReturn(tripResponse1);

        Page<TripResponse> result = tripService.getAllTrips(pageable, 1L, null, "Origin Address", "Destination Address", "CREATED");

        assertEquals(1, result.getTotalElements());
        List<TripResponse> responses = result.getContent();
        assertEquals(1, responses.size());
        assertEquals(tripResponse1, responses.get(0));

        verify(tripRepository).findAll(any(Example.class), eq(pageable));
    }

}
