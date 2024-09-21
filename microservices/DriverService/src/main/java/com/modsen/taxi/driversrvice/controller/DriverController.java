package com.modsen.taxi.driversrvice.controller;

import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
import com.modsen.taxi.driversrvice.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/drivers")
@Validated
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> getAllDrivers(@RequestParam(required = false) String firstName,
                                                                   @RequestParam(required = false) String lastName,
                                                                   @RequestParam(required = false) String phone,
                                                                   @RequestParam(defaultValue = "true") boolean isActive,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size,
                                                                   @RequestParam(defaultValue = "id,asc") String sort) {
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(sortParams[0]).ascending();
        if ("desc".equalsIgnoreCase(sortParams[1])) {
            sortOrder = Sort.by(sortParams[0]).descending();
        }
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        return driverService.getAllDrivers(pageable, firstName, lastName, phone, isActive)
                .map(pageDrivers -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("drivers", pageDrivers.getContent());
                    response.put("currentPage", pageDrivers.getNumber());
                    response.put("totalItems", pageDrivers.getTotalElements());
                    response.put("totalPages", pageDrivers.getTotalPages());
                    return new ResponseEntity<>(response, HttpStatus.OK);
                });
    }

    @PostMapping
    public Mono<ResponseEntity<DriverResponse>> createDriver(@Validated @RequestBody DriverRequest driverRequest) {
        return driverService.createDriver(driverRequest)
                .map(driver -> new ResponseEntity<>(driver, HttpStatus.CREATED));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<DriverResponse>> updateDriver(@PathVariable Long id, @Validated @RequestBody DriverRequest driverRequest) {
        return driverService.updateDriver(id, driverRequest)
                .map(updatedDriver -> new ResponseEntity<>(updatedDriver, HttpStatus.OK));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteDriver(@PathVariable Long id) {
        return driverService.deleteDriver(id)
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<DriverResponse>> getDriverById(@PathVariable Long id) {
        return driverService.getDriverById(id)
                .map(driver -> new ResponseEntity<>(driver, HttpStatus.OK));
    }
}
