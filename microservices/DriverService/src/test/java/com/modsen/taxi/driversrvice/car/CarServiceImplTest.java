package com.modsen.taxi.driversrvice.car;

import com.modsen.taxi.driversrvice.domain.Car;
import com.modsen.taxi.driversrvice.dto.request.CreateCarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import com.modsen.taxi.driversrvice.error.exception.DuplicateResourceException;
import com.modsen.taxi.driversrvice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.driversrvice.mapper.CarMapper;
import com.modsen.taxi.driversrvice.repository.CarRepository;
import com.modsen.taxi.driversrvice.service.impl.CarServiceImpl;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarServiceImplTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @Mock
    private Scheduler jdbcScheduler;

    @InjectMocks
    private CarServiceImpl carService;

    private CreateCarRequest createCarRequest;
    private Car car1;
    private Car car2;
    private CarResponse carResponse1;
    private CarResponse carResponse2;
    private Pageable pageable;

    @BeforeEach
    public void setUp() {
        when(jdbcScheduler.schedule(any())).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return mock(Disposable.class);
        });

        createCarRequest = new CreateCarRequest("BMW", "Blue", "ABC123");
        car1 = new Car(1L, "BMW", "Blue", "ABC123", false, null);
        car2 = new Car(2L, "Audi", "Red", "XYZ789", false, null);
        carResponse1 = new CarResponse(1L, "BMW", "Blue", "ABC123");
        carResponse2 = new CarResponse(2L, "Audi", "Red", "XYZ789");
        pageable = PageRequest.of(0, 2);
    }

    @Test
    void createCar_ShouldReturnCarResponse_WhenCarCreatedSuccessfully() {
        when(carRepository.existsByLicensePlate(anyString())).thenReturn(false);
        when(carMapper.toCar(createCarRequest)).thenReturn(car1);
        when(carRepository.save(car1)).thenReturn(car1);
        when(carMapper.toCarResponse(car1)).thenReturn(carResponse1);

        Mono<CarResponse> result = carService.createCar(createCarRequest);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.licensePlate().equals("ABC123"))
                .verifyComplete();

        verify(carRepository).existsByLicensePlate(anyString());
        verify(carRepository).save(car1);
    }

    @Test
    void createCar_ShouldThrowDuplicateResourceException_WhenCarAlreadyExists() {
        when(carRepository.existsByLicensePlate(anyString())).thenReturn(true);

        Mono<CarResponse> result = carService.createCar(createCarRequest);

        StepVerifier.create(result)
                .expectError(DuplicateResourceException.class)
                .verify();

        verify(carRepository).existsByLicensePlate(anyString());
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    void getCarById_ShouldReturnCarResponse_WhenCarExists() {
        when(carRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(car1));
        when(carMapper.toCarResponse(car1)).thenReturn(carResponse1);

        Mono<CarResponse> result = carService.getCarById(1L);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.licensePlate().equals("ABC123"))
                .verifyComplete();

        verify(carRepository).findByIdAndIsDeletedFalse(anyLong());
    }

    @Test
    void getCarById_ShouldThrowResourceNotFoundException_WhenCarNotFound() {
        when(carRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.empty());

        Mono<CarResponse> result = carService.getCarById(1L);

        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(carRepository).findByIdAndIsDeletedFalse(anyLong());
    }

    @Test
    void deleteCar_ShouldMarkCarAsDeleted_WhenCarExists() {
        when(carRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(car1));

        Mono<Void> result = carService.deleteCar(1L);

        StepVerifier.create(result)
                .verifyComplete();

        verify(carRepository).findByIdAndIsDeletedFalse(anyLong());
        assert car1.getIsDeleted();
        verify(carRepository).save(car1);
    }

    @Test
    void deleteCar_ShouldThrowResourceNotFoundException_WhenCarDoesNotExist() {
        when(carRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.empty());

        Mono<Void> result = carService.deleteCar(1L);

        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    void getAllCars_ShouldReturnPagedCars_WhenCarsExist() {
        Page<Car> carPage = new PageImpl<>(List.of(car1, car2), pageable, 2);
        when(carRepository.findAll(any(Example.class), any(Pageable.class))).thenReturn(carPage);
        when(carMapper.toCarResponse(car1)).thenReturn(carResponse1);
        when(carMapper.toCarResponse(car2)).thenReturn(carResponse2);

        Mono<Page<CarResponse>> result = carService.getAllCars(pageable, null, null, null, true);

        StepVerifier.create(result)
                .expectNextMatches(page -> {
                    assertEquals(2, page.getSize());
                    assertEquals(2, page.getTotalElements());
                    List<CarResponse> responses = page.getContent();
                    return responses.size() == 2 &&
                            responses.get(0).equals(carResponse1) &&
                            responses.get(1).equals(carResponse2);
                })
                .verifyComplete();

        verify(carRepository).findAll(any(Example.class), eq(pageable));
    }

    @Test
    void getAllCars_ShouldReturnFilteredCars_WhenFiltersAreApplied() {
        Page<Car> carPage = new PageImpl<>(List.of(car1), pageable, 1);
        when(carRepository.findAll(any(Example.class), eq(pageable))).thenReturn(carPage);
        when(carMapper.toCarResponse(car1)).thenReturn(carResponse1);

        Mono<Page<CarResponse>> result = carService.getAllCars(pageable, "BMW", "", "", true);

        StepVerifier.create(result)
                .expectNextMatches(page -> {
                    List<CarResponse> cars = page.getContent();
                    return cars.size() == 1 &&
                            cars.get(0).brand().equals("BMW") &&
                            cars.get(0).color().equals("Blue");
                })
                .verifyComplete();

        verify(carRepository).findAll(any(Example.class), eq(pageable));
        verify(carMapper).toCarResponse(car1);
    }
}
