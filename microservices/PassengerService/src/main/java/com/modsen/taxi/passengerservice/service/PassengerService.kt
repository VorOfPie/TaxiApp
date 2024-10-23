package com.modsen.taxi.passengerservice.service

import com.modsen.taxi.passengerservice.dto.PassengerRequest
import com.modsen.taxi.passengerservice.dto.PassengerResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Mono

interface PassengerService {
    fun createPassenger(passengerRequest: PassengerRequest): Mono<PassengerResponse>
    fun updatePassenger(id: Long, passengerRequest: PassengerRequest): Mono<PassengerResponse>
    fun getPassengerById(id: Long): Mono<PassengerResponse>
    fun getAllPassengers(
        pageable: Pageable,
        firstName: String?,
        lastName: String?,
        email: String?,
        isActive: Boolean?
    ): Mono<Page<PassengerResponse>>

    fun deletePassenger(id: Long): Mono<Void>
}
