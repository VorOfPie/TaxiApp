package com.modsen.taxi.driversrvice.controller;

import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
import com.modsen.taxi.driversrvice.service.DriverService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/drivers")
@Validated
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllDrivers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false, defaultValue = "true") boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sort[0]).ascending());
            if ("desc".equalsIgnoreCase(sort[1])) {
                pageable = PageRequest.of(page, size, Sort.by(sort[0]).descending());
            }

            Page<DriverResponse> pageDrivers = driverService.getAllDrivers(pageable, firstName, lastName, phone, isActive);

            Map<String, Object> response = new HashMap<>();
            response.put("drivers", pageDrivers.getContent());
            response.put("currentPage", pageDrivers.getNumber());
            response.put("totalItems", pageDrivers.getTotalElements());
            response.put("totalPages", pageDrivers.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable Long id) {
        DriverResponse driverResponse = driverService.getDriverById(id);
        return new ResponseEntity<>(driverResponse, HttpStatus.OK);

    }

    @PostMapping
    public ResponseEntity<DriverResponse> createDriver(@Validated @RequestBody DriverRequest driverRequest) {
        try {
            DriverResponse driverResponse = driverService.createDriver(driverRequest);
            return new ResponseEntity<>(driverResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<DriverResponse> updateDriver(
            @PathVariable Long id,
            @Validated @RequestBody DriverRequest driverRequest) {

        DriverResponse updatedDriver = driverService.updateDriver(id, driverRequest);
        return new ResponseEntity<>(updatedDriver, HttpStatus.OK);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteDriver(@PathVariable Long id) {

        driverService.deleteDriver(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
