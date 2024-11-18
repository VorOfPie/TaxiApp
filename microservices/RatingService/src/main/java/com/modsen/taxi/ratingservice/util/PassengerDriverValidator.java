package com.modsen.taxi.ratingservice.util;

import com.modsen.taxi.ratingservice.config.DriverClient;
import com.modsen.taxi.ratingservice.config.PassengerClient;
import com.modsen.taxi.ratingservice.dto.response.DriverResponse;
import com.modsen.taxi.ratingservice.dto.response.PassengerResponse;
import com.modsen.taxi.ratingservice.error.exception.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

import static feign.FeignException.Forbidden;

@Component
@RequiredArgsConstructor
public class PassengerDriverValidator {

    private final PassengerClient passengerClient;
    private final DriverClient driverClient;

    public void validatePassengerAndDriverExistence(Long passengerId, Long driverId) {
        if (!doesPassengerExist(passengerId) || !doesDriverExist(driverId)) {
            throw new AccessDeniedException("Passenger or Driver not found");
        }
    }

    public void validatePassengerAndDriverAccess(Long passengerId, Long driverId) {
        if (!isPassengerAccessible(passengerId) && !isDriverAccessible(driverId)) {
            throw new AccessDeniedException("Access is denied");
        }
    }

    private boolean doesPassengerExist(Long passengerId) {
        try {
            passengerClient.getPassengerById(passengerId);
            return true;
        } catch (Forbidden ignored) {
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean doesDriverExist(Long driverId) {
        try {
            driverClient.getDriverById(driverId);
            return true;
        } catch (Forbidden ignored) {
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isPassengerAccessible(Long passengerId) {
        if (isAdmin()) {
            return true;
        }
        try {
            PassengerResponse passenger = passengerClient.getPassengerById(passengerId);
            return passenger != null;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isDriverAccessible(Long driverId) {
        if (isAdmin()) {
            return true;
        }
        try {
            DriverResponse driver = driverClient.getDriverById(driverId);
            return driver != null;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return false;
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        var roles = (List<String>) jwt.getClaimAsMap("realm_access").get("roles");
        return roles != null && roles.contains("ROLE_ADMIN");
    }
}
