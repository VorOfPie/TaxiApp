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
import com.modsen.taxi.tripservice.error.exception.InvalidRequestException;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.GenericMessage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

        assertEquals("rating-topic", capturedMessage.getHeaders().get(KafkaHeaders.TOPIC)); // Check the topic
        assertEquals(1L, capturedRatingRequest.driverId());
        assertEquals(1L, capturedRatingRequest.passengerId());
        assertEquals(5.0, capturedRatingRequest.score());
        assertEquals("Great trip!", capturedRatingRequest.comment());

        assertEquals(TripStatus.COMPLETED, trip.getStatus());
        verify(tripRepository).save(trip);
    }


    @Test
    void closeAndRateTrip_ShouldThrowInvalidRequestException_WhenKafkaSendFails() {
        ScoreRequest scoreRequest = new ScoreRequest(5.0, "Great trip!");
        when(tripRepository.findById(anyLong())).thenReturn(Optional.of(trip));

        doThrow(new RuntimeException("Kafka error")).when(kafkaTemplate).send(anyString(), any(RatingRequest.class));

        Exception exception = assertThrows(InvalidRequestException.class, () -> tripService.closeAndRateTrip(1L, scoreRequest));

        assertEquals("Failed to send rating event via Kafka", exception.getMessage());
        verify(tripRepository).save(trip);
    }
}
