package com.modsen.taxi.tripservice.domain;

public enum TripStatus {
    CREATED,
    ACCEPTED,
    EN_ROUTE_TO_PASSENGER,
    EN_ROUTE_TO_DESTINATION,
    COMPLETED,
    CANCELLED
}
