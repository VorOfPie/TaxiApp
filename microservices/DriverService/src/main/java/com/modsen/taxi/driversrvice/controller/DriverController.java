package com.modsen.taxi.driversrvice.controller;

import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.request.DriverUpdateRequest;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
import com.modsen.taxi.driversrvice.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    public Mono<ResponseEntity<DriverResponse>> createDriver(@Valid @RequestBody DriverRequest driverRequest) {
        return driverService.createDriver(driverRequest)
                .map(driver -> new ResponseEntity<>(driver, HttpStatus.CREATED));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Mono<ResponseEntity<DriverResponse>> updateDriver(@PathVariable Long id,
                                                             @Valid @RequestBody DriverUpdateRequest driverUpdateRequest,
                                                             @AuthenticationPrincipal Jwt jwt) {
        String principalEmail = jwt.getClaim("email");
        boolean isAdmin = isAdmin(jwt);
        return driverService.updateDriver(id, driverUpdateRequest, principalEmail, isAdmin)
                .map(updatedDriver -> new ResponseEntity<>(updatedDriver, HttpStatus.OK));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Mono<ResponseEntity<DriverResponse>> getDriverById(@PathVariable Long id,
                                                              @AuthenticationPrincipal Jwt jwt) {
        String principalEmail = jwt.getClaim("email");
        boolean isAdmin = isAdmin(jwt);
        return driverService.getDriverById(id, principalEmail, isAdmin)
                .map(driver -> new ResponseEntity<>(driver, HttpStatus.OK));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Mono<ResponseEntity<Void>> deleteDriver(@PathVariable Long id,
                                                   @AuthenticationPrincipal Jwt jwt) {
        String principalEmail = jwt.getClaim("email");
        boolean isAdmin = isAdmin(jwt);
        return driverService.deleteDriver(id, principalEmail, isAdmin)
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    private boolean isAdmin(Jwt jwt) {
        var roles = (List<String>) jwt.getClaimAsMap("realm_access").get("roles");
        return roles.contains("ROLE_ADMIN");
    }
}
