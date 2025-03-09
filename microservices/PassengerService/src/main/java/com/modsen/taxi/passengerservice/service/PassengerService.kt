package com.modsen.taxi.passengerservice.service

import com.modsen.taxi.passengerservice.dto.PassengerRequest
import com.modsen.taxi.passengerservice.dto.PassengerResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Mono

interface PassengerService {
    fun createPassenger(passengerRequest: PassengerRequest): Mono<PassengerResponse>

    fun updatePassenger(
        id: Long,
        passengerUpdateRequest: PassengerRequest,
        principalEmail: String,
        isAdmin: Boolean
    ): Mono<PassengerResponse>

    fun getPassengerById(id: Long, principalEmail: String, isAdmin: Boolean): Mono<PassengerResponse>

    fun getAllPassengers(
        pageable: Pageable,
        firstName: String?,
        lastName: String?,
        email: String?,
        isActive: Boolean
    ): Mono<Page<PassengerResponse>>

    fun deletePassenger(id: Long, principalEmail: String, isAdmin: Boolean): Mono<Void>
}
