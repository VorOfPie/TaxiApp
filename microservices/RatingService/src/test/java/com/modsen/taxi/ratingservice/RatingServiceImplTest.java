package com.modsen.taxi.ratingservice;

import com.modsen.taxi.ratingservice.config.DriverClient;
import com.modsen.taxi.ratingservice.config.PassengerClient;
import com.modsen.taxi.ratingservice.domain.Rating;
import com.modsen.taxi.ratingservice.dto.RatingRequest;
import com.modsen.taxi.ratingservice.dto.response.DriverResponse;
import com.modsen.taxi.ratingservice.dto.response.PassengerResponse;
import com.modsen.taxi.ratingservice.dto.response.RatingResponse;
import com.modsen.taxi.ratingservice.error.exception.DuplicateResourceException;
import com.modsen.taxi.ratingservice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.ratingservice.mapper.RatingMapper;
import com.modsen.taxi.ratingservice.repository.RatingRepository;
import com.modsen.taxi.ratingservice.service.impl.RatingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RatingServiceImplTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private RatingMapper ratingMapper;

    @Mock
    private PassengerClient passengerClient;

    @Mock
    private DriverClient driverClient;

    @InjectMocks
    private RatingServiceImpl ratingService;

    private RatingRequest ratingRequest;
    private Rating rating;
    private RatingResponse ratingResponse;
    private PassengerResponse passengerResponse;
    private DriverResponse driverResponse;

    private Rating rating1;
    private Rating rating2;
    private RatingResponse ratingResponse1;
    private RatingResponse ratingResponse2;
    private Pageable pageable;

    @BeforeEach
    public void setUp() {
        ratingRequest = new RatingRequest(1L, 1L, 5.0, "Great service!");
        rating = new Rating(1L, 1L, 1L, 5.0, "Great service!");
        ratingResponse = new RatingResponse(1L, 1L, 1L, 5.0, "Great service!");

        rating1 = new Rating(1L, 1L, 1L, 5.0, "Great service!");
        rating2 = new Rating(2L, 2L, 1L, 4.0, "Good service!");

        ratingResponse1 = new RatingResponse(1L, 1L, 1L, 5.0, "Great service!");
        ratingResponse2 = new RatingResponse(2L, 1L, 1L, 4.0, "Good service!");

        pageable = PageRequest.of(0, 2); // First page with size 2

        passengerResponse = new PassengerResponse(1L, "Passenger Name", "Last Name", "passenger@example.com", "1234567890");
        driverResponse = new DriverResponse(1L, "Driver Name", "Last Name", "driver@example.com", "Male", null);
    }

    @Test
    void createRating_ShouldReturnRatingResponse_WhenRatingCreatedSuccessfully() {
        when(passengerClient.getPassengerById(anyLong())).thenReturn(passengerResponse);
        when(driverClient.getDriverById(anyLong())).thenReturn(driverResponse);
        when(ratingRepository.existsByDriverIdAndPassengerId(anyLong(), anyLong())).thenReturn(false);
        when(ratingMapper.toRating(any(RatingRequest.class))).thenReturn(rating);
        when(ratingRepository.save(any(Rating.class))).thenReturn(rating);
        when(ratingMapper.toRatingResponse(any(Rating.class))).thenReturn(ratingResponse);

        RatingResponse result = ratingService.createRating(ratingRequest);

        assertEquals(ratingResponse, result);
        verify(ratingRepository).save(rating);
    }

    @Test
    void createRating_ShouldThrowDuplicateResourceException_WhenRatingAlreadyExists() {
        when(ratingRepository.existsByDriverIdAndPassengerId(anyLong(), anyLong())).thenReturn(true);

        Exception exception = assertThrows(DuplicateResourceException.class, () -> ratingService.createRating(ratingRequest));

        assertEquals("Rating for this driver and passenger already exists.", exception.getMessage());
        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    void updateRating_ShouldReturnUpdatedRatingResponse_WhenRatingExists() {
        when(ratingRepository.findById(anyLong())).thenReturn(Optional.of(rating));
        when(passengerClient.getPassengerById(anyLong())).thenReturn(passengerResponse);
        when(driverClient.getDriverById(anyLong())).thenReturn(driverResponse);
        when(ratingRepository.save(any(Rating.class))).thenReturn(rating);
        when(ratingMapper.toRatingResponse(any(Rating.class))).thenReturn(ratingResponse);

        RatingResponse result = ratingService.updateRating(1L, ratingRequest);

        assertEquals(ratingResponse, result);
        verify(ratingRepository).save(rating);
    }

    @Test
    void updateRating_ShouldThrowResourceNotFoundException_WhenRatingNotFound() {
        when(ratingRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> ratingService.updateRating(1L, ratingRequest));

        assertEquals("Rating with id 1 not found.", exception.getMessage());
        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    void getRatingById_ShouldReturnRatingResponse_WhenRatingExists() {
        when(ratingRepository.findById(anyLong())).thenReturn(Optional.of(rating));
        when(ratingMapper.toRatingResponse(any(Rating.class))).thenReturn(ratingResponse);

        RatingResponse result = ratingService.getRatingById(1L);

        assertEquals(ratingResponse, result);
        verify(ratingRepository).findById(anyLong());
    }

    @Test
    void getRatingById_ShouldThrowResourceNotFoundException_WhenRatingNotFound() {
        when(ratingRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> ratingService.getRatingById(1L));

        assertEquals("Rating with id 1 not found.", exception.getMessage());
        verify(ratingRepository).findById(anyLong());
    }

    @Test
    void deleteRating_ShouldDeleteRating_WhenRatingExists() {
        when(ratingRepository.findById(anyLong())).thenReturn(Optional.of(rating));

        ratingService.deleteRating(1L);

        verify(ratingRepository).delete(rating);
    }

    @Test
    void deleteRating_ShouldThrowResourceNotFoundException_WhenRatingNotFound() {
        when(ratingRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> ratingService.deleteRating(1L));

        assertEquals("Rating with id 1 not found.", exception.getMessage());
        verify(ratingRepository, never()).delete(any(Rating.class));
    }

    @Test
    void getAllRatings_ShouldReturnPagedRatings_WhenRatingsExist() {
        Page<Rating> ratingPage = new PageImpl<>(List.of(rating1, rating2), pageable, 2);
        when(ratingRepository.findAll(any(Example.class), any(Pageable.class))).thenReturn(ratingPage);
        when(ratingMapper.toRatingResponse(rating1)).thenReturn(ratingResponse1);
        when(ratingMapper.toRatingResponse(rating2)).thenReturn(ratingResponse2);

        Page<RatingResponse> result = ratingService.getAllRatings(pageable, null, null);

        assertEquals(2, result.getSize());
        assertEquals(2, result.getTotalElements());
        List<RatingResponse> responses = result.getContent();
        assertEquals(2, responses.size());
        assertEquals(ratingResponse1, responses.get(0));
        assertEquals(ratingResponse2, responses.get(1));

        verify(ratingRepository).findAll(any(Example.class), eq(pageable));
    }

    @Test
    void getAllRatings_ShouldReturnFilteredRatings_WhenFiltersAreApplied() {
        Page<Rating> ratingPage = new PageImpl<>(List.of(rating1), pageable, 1);
        when(ratingRepository.findAll(any(Example.class), eq(pageable))).thenReturn(ratingPage);
        when(ratingMapper.toRatingResponse(rating1)).thenReturn(ratingResponse1);

        Page<RatingResponse> result = ratingService.getAllRatings(pageable, 1L, null); // Assuming 1L is a valid tripId

        assertEquals(1, result.getTotalElements());
        List<RatingResponse> responses = result.getContent();
        assertEquals(1, responses.size());
        assertEquals(ratingResponse1, responses.get(0));

        verify(ratingRepository).findAll(any(Example.class), eq(pageable));
    }

}
