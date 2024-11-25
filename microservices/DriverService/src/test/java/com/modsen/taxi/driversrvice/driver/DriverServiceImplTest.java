package com.modsen.taxi.driversrvice.driver;

import com.modsen.taxi.driversrvice.domain.Car;
import com.modsen.taxi.driversrvice.domain.Driver;
import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
import com.modsen.taxi.driversrvice.error.exception.AccessDeniedException;
import com.modsen.taxi.driversrvice.error.exception.DuplicateResourceException;
import com.modsen.taxi.driversrvice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.driversrvice.mapper.DriverMapper;
import com.modsen.taxi.driversrvice.repository.CarRepository;
import com.modsen.taxi.driversrvice.repository.DriverRepository;
import com.modsen.taxi.driversrvice.service.impl.DriverServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.*;
import org.springframework.security.test.context.support.WithMockUser;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DriverServiceImplTest {

    @Mock
    private DriverRepository driverRepository;
    @Mock
    private DriverMapper driverMapper;
    @Mock
    private CarRepository carRepository;

    @Mock
    private Scheduler jdbcScheduler;
    @InjectMocks
    private DriverServiceImpl driverService;

    private DriverRequest driverRequest;
    private Driver driver1;
    private Driver driver2;
    private DriverResponse driverResponse1;
    private DriverResponse driverResponse2;
    private Pageable pageable;

    @BeforeEach
    @Profile("test")
    public void setUp() {
        when(jdbcScheduler.schedule(any())).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return mock(Disposable.class);
        });
        driverRequest = new DriverRequest("John", "Doe", "1234567890", "john.doe@example.com", "Male", new ArrayList<>());
        driver1 = new Driver(1L, "John", "Doe", "1234567890", "john.doe@example.com", "Male", false, List.of(new Car()));
        driver2 = new Driver(2L, "Jane", "Doe", "0987654321", "jane.doe@gmail.com", "Female", false, null);
        driverResponse1 = new DriverResponse(1L, "John", "Doe", "1234567890", "john.doe@example.com", "Male", null);
        driverResponse2 = new DriverResponse(2L, "Jane", "Doe", "0987654321", "jane.doe@gmail.com", "Female", null);
        pageable = PageRequest.of(0, 2);
    }

    @Test
    void createDriver_ShouldReturnDriverResponse_WhenDriverCreatedSuccessfully() {
        when(driverMapper.toDriver(driverRequest)).thenReturn(driver1);
        when(driverRepository.save(driver1)).thenReturn(driver1);
        when(driverMapper.toDriverResponse(driver1)).thenReturn(driverResponse1);
        Mono<DriverResponse> result = driverService.createDriver(driverRequest);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.phone().equals("1234567890"))
                .verifyComplete();

        verify(driverRepository, times(1)).save(driver1);
    }

    @Test
    void createDriver_ShouldThrowDuplicateResourceException_WhenDriverAlreadyExists() {
        when(driverRepository.existsByPhone(anyString())).thenReturn(true);
        Mono<DriverResponse> result = driverService.createDriver(driverRequest);

        StepVerifier.create(result)
                .expectError(DuplicateResourceException.class)
                .verify();

        verify(driverRepository).existsByPhone(anyString());
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com", roles = {"USER"})
    void getDriverById_ShouldReturnDriverResponse_WhenDriverExistsAndHasAccess() {
        when(driverRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(driver1));
        when(driverMapper.toDriverResponse(driver1)).thenReturn(driverResponse1);

        Mono<DriverResponse> result = driverService.getDriverById(1L, "john.doe@example.com", false);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.email().equals("john.doe@example.com"))
                .verifyComplete();

        verify(driverRepository).findByIdAndIsDeletedFalse(anyLong());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com", roles = {"USER"})
    void getDriverById_ShouldThrowAccessDeniedException_WhenUserHasNoAccess() {
        when(driverRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(driver1));

        Mono<DriverResponse> result = driverService.getDriverById(1L, "authorized@example.com", false);

        StepVerifier.create(result)
                .expectError(AccessDeniedException.class)
                .verify();

        verify(driverRepository).findByIdAndIsDeletedFalse(anyLong());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com", roles = {"USER"})
    void updateDriver_ShouldReturnDriverResponse_WhenDriverUpdatedSuccessfully() {
        when(driverRepository.findById(anyLong())).thenReturn(Optional.of(driver1));
        when(driverRepository.save(driver1)).thenReturn(driver1);
        when(driverMapper.toDriverResponse(driver1)).thenReturn(driverResponse1);

        Mono<DriverResponse> result = driverService.updateDriver(1L, driverRequest, "john.doe@example.com", false);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.phone().equals("1234567890"))
                .verifyComplete();

        verify(driverRepository).findById(anyLong());
        verify(driverRepository).save(driver1);
    }


    @Test
    @WithMockUser(username = "john.doe@example.com", roles = {"USER"})
    void updateDriver_ShouldThrowResourceNotFoundException_WhenDriverNotFound() {
        when(driverRepository.findById(anyLong())).thenReturn(Optional.empty());

        Mono<DriverResponse> result = driverService.updateDriver(1L, driverRequest, "john.doe@example.com", false);

        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(driverRepository).findById(anyLong());
        verify(driverRepository, never()).save(any());
    }


    @Test
    @WithMockUser(username = "john.doe@example.com", roles = {"ADMIN"})
    void deleteDriver_ShouldMarkDriverAsDeleted_WhenAdminDeletes() {
        when(driverRepository.findById(anyLong())).thenReturn(Optional.of(driver1));

        Mono<Void> result = driverService.deleteDriver(1L, "john.doe@example.com", true);

        StepVerifier.create(result)
                .verifyComplete();

        verify(driverRepository).save(driver1);
        assertTrue(driver1.getIsDeleted());
    }

    @Test
    @WithMockUser(username = "jane.doe@gmail.com", roles = {"USER"})
    void deleteDriver_ShouldThrowAccessDeniedException_WhenNonAdminTriesToDelete() {
        when(driverRepository.findById(anyLong())).thenReturn(Optional.of(driver1));

        Mono<Void> result = driverService.deleteDriver(1L, "jane.doe@gmail.com", false);

        StepVerifier.create(result)
                .expectError(AccessDeniedException.class)
                .verify();

        verify(driverRepository, never()).save(any());
    }

    @Test
    @WithMockUser(username = "jane.doe@gmail.com", roles = {"ADMIN"})
    void getAllDrivers_ShouldReturnPagedDrivers_WhenDriversExist() {
        Page<Driver> driverPage = new PageImpl<>(List.of(driver1, driver2), pageable, 2);
        when(driverRepository.findAll(any(Example.class), any(Pageable.class))).thenReturn(driverPage);
        when(driverMapper.toDriverResponse(driver1)).thenReturn(driverResponse1);
        when(driverMapper.toDriverResponse(driver2)).thenReturn(driverResponse2);

        Mono<Page<DriverResponse>> result = driverService.getAllDrivers(pageable, null, null, null, true);

        StepVerifier.create(result)
                .expectNextMatches(page -> {
                    assertEquals(2, page.getSize());
                    assertEquals(2, page.getTotalElements());
                    List<DriverResponse> responses = page.getContent();
                    return responses.size() == 2 &&
                            responses.get(0).equals(driverResponse1) &&
                            responses.get(1).equals(driverResponse2);
                })
                .verifyComplete();

        verify(driverRepository).findAll(any(Example.class), eq(pageable));
    }

    @Test
    @WithMockUser(username = "jane.doe@gmail.com", roles = {"ADMIN"})
    void getAllDrivers_ShouldReturnFilteredDrivers_WhenFiltersAreApplied() {
        Page<Driver> driverPage = new PageImpl<>(List.of(driver1), pageable, 1);
        when(driverRepository.findAll(any(Example.class), eq(pageable))).thenReturn(driverPage);
        when(driverMapper.toDriverResponse(driver1)).thenReturn(driverResponse1);

        Mono<Page<DriverResponse>> result = driverService.getAllDrivers(pageable, "John", "", "", true);

        StepVerifier.create(result)
                .expectNextMatches(page -> {
                    List<DriverResponse> drivers = page.getContent();
                    return drivers.size() == 1 &&
                            drivers.get(0).firstName().equals("John") &&
                            drivers.get(0).lastName().equals("Doe");
                })
                .verifyComplete();

        verify(driverRepository).findAll(any(Example.class), eq(pageable));
        verify(driverMapper).toDriverResponse(driver1);
    }
}