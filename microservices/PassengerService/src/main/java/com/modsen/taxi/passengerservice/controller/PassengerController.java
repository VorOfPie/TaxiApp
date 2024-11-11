package com.modsen.taxi.passengerservice.controller;

import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;
import com.modsen.taxi.passengerservice.dto.PassengerUpdateRequest;
import com.modsen.taxi.passengerservice.service.PassengerService;
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
@RequestMapping("/api/v1/passengers")
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerService passengerService;

    @PostMapping
    public Mono<ResponseEntity<PassengerResponse>> createPassenger(@Valid @RequestBody PassengerRequest passengerRequest) {
        return passengerService.createPassenger(passengerRequest)
                .map(passenger -> new ResponseEntity<>(passenger, HttpStatus.CREATED));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Mono<ResponseEntity<PassengerResponse>> updatePassenger(@PathVariable Long id,
                                                                   @Valid @RequestBody PassengerUpdateRequest passengerUpdateRequest,
                                                                   @AuthenticationPrincipal Jwt jwt) {
        String principalEmail = jwt.getClaim("email");
        boolean isAdmin = isAdmin(jwt);
        return passengerService.updatePassenger(id, passengerUpdateRequest, principalEmail, isAdmin)
                .map(updatedPassenger -> new ResponseEntity<>(updatedPassenger, HttpStatus.OK));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Mono<ResponseEntity<PassengerResponse>> getPassengerById(@PathVariable Long id,
                                                                    @AuthenticationPrincipal Jwt jwt) {
        String principalEmail = jwt.getClaim("email");
        boolean isAdmin = isAdmin(jwt);
        return passengerService.getPassengerById(id, principalEmail, isAdmin)
                .map(passenger -> new ResponseEntity<>(passenger, HttpStatus.OK));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Map<String, Object>>> getAllPassengers(@RequestParam(required = false) String firstName,
                                                                      @RequestParam(required = false) String lastName,
                                                                      @RequestParam(required = false) String email,
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

        return passengerService.getAllPassengers(pageable, firstName, lastName, email, isActive)
                .map(pagePassengers -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("passengers", pagePassengers.getContent());
                    response.put("currentPage", pagePassengers.getNumber());
                    response.put("totalItems", pagePassengers.getTotalElements());
                    response.put("totalPages", pagePassengers.getTotalPages());
                    return new ResponseEntity<>(response, HttpStatus.OK);
                });
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Mono<ResponseEntity<Void>> deletePassenger(@PathVariable Long id,
                                                      @AuthenticationPrincipal Jwt jwt) {
        String principalEmail = jwt.getClaim("email");
        boolean isAdmin = isAdmin(jwt);
        return passengerService.deletePassenger(id, principalEmail, isAdmin)
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    private boolean isAdmin(Jwt jwt) {
        var roles = (List<String>) jwt.getClaimAsMap("realm_access").get("roles");
        return roles.contains("ROLE_ADMIN");
    }
}

