package com.modsen.taxi.passengerservice.error

import com.modsen.taxi.passengerservice.dto.error.AppError
import com.modsen.taxi.passengerservice.dto.error.AppErrorCustom
import com.modsen.taxi.passengerservice.error.exception.AccessDeniedException
import com.modsen.taxi.passengerservice.error.exception.DuplicateResourceException
import com.modsen.taxi.passengerservice.error.exception.InvalidRequestException
import com.modsen.taxi.passengerservice.error.exception.ResourceNotFoundException
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private const val DEFAULT_ERROR_MESSAGE = "No message available"
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleResourceNotFound(e: ResourceNotFoundException): AppError {
        return AppError(
            status = HttpStatus.NOT_FOUND.value(),
            message = e.message ?: DEFAULT_ERROR_MESSAGE,
            timestamp = LocalDateTime.now()
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentNotValid(e: MethodArgumentNotValidException): AppErrorCustom {
        val errors = e.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: DEFAULT_ERROR_MESSAGE)
        }
        return AppErrorCustom(
            status = HttpStatus.BAD_REQUEST.value(),
            message = e.message ?: DEFAULT_ERROR_MESSAGE,
            timestamp = LocalDateTime.now(),
            errors = errors
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleConstraintViolation(e: ConstraintViolationException): AppErrorCustom {
        val errors = e.constraintViolations.associate {
            it.propertyPath.toString() to it.message
        }
        return AppErrorCustom(
            status = HttpStatus.BAD_REQUEST.value(),
            message = e.message ?: DEFAULT_ERROR_MESSAGE,
            timestamp = LocalDateTime.now(),
            errors = errors
        )
    }

    @ExceptionHandler(DuplicateResourceException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDuplicateResourceException(e: DuplicateResourceException): AppError {
        return AppError(
            status = HttpStatus.CONFLICT.value(),
            message = e.message ?: DEFAULT_ERROR_MESSAGE,
            timestamp = LocalDateTime.now()
        )
    }

    @ExceptionHandler(InvalidRequestException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidRequestException(e: InvalidRequestException): AppError {
        return AppError(
            status = HttpStatus.BAD_REQUEST.value(),
            message = e.message ?: DEFAULT_ERROR_MESSAGE,
            timestamp = LocalDateTime.now()
        )
    }

    @ExceptionHandler(AccessDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun onAccessDeniedException(e: AccessDeniedException): AppError {
        return AppError(
            status = HttpStatus.FORBIDDEN.value(),
            message = e.message ?: DEFAULT_ERROR_MESSAGE,
            timestamp = LocalDateTime.now()
        )
    }
}
