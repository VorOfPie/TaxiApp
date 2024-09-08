package com.modsen.taxi.tripservice.error;


import com.modsen.taxi.tripservice.dto.error.AppError;
import com.modsen.taxi.tripservice.dto.error.AppErrorCustom;
import com.modsen.taxi.tripservice.error.exception.DuplicateResourceException;
import com.modsen.taxi.tripservice.error.exception.InvalidRequestException;
import com.modsen.taxi.tripservice.error.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String DEFAULT_ERROR_MESSAGE = "No message available";


    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public AppError handleResourceNotFound(ResourceNotFoundException e) {
        return AppError.builder().status(HttpStatus.NOT_FOUND.value()).message(e.getMessage()).timestamp(LocalDateTime.now()).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AppErrorCustom handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, error -> Objects.requireNonNullElse(error.getDefaultMessage(), DEFAULT_ERROR_MESSAGE)));
        return AppErrorCustom.builder().status(HttpStatus.BAD_REQUEST.value()).message(e.getMessage()).timestamp(LocalDateTime.now()).errors(errors).build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AppErrorCustom handleConstraintViolation(ConstraintViolationException e) {
        Map<String, String> errors = e.getConstraintViolations().stream().collect(Collectors.toMap(violation -> violation.getPropertyPath().toString(), ConstraintViolation::getMessage));
        return AppErrorCustom.builder().status(HttpStatus.BAD_REQUEST.value()).message(e.getMessage()).timestamp(LocalDateTime.now()).errors(errors).build();
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public AppError handleDuplicateResourceException(DuplicateResourceException e) {
        return AppError.builder().status(HttpStatus.CONFLICT.value()).message(e.getMessage()).timestamp(LocalDateTime.now()).build();
    }


    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AppError handleInvalidRequestException(InvalidRequestException e) {
        return AppError.builder().status(HttpStatus.BAD_REQUEST.value()).message(e.getMessage()).timestamp(LocalDateTime.now()).build();
    }
}
