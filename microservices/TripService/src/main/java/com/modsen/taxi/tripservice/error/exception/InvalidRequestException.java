package com.modsen.taxi.tripservice.error.exception;


public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
