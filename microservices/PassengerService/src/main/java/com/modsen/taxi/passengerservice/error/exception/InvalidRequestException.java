package com.modsen.taxi.passengerservice.error.exception;


public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
