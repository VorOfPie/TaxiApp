package com.modsen.taxi.passengerservice.service.impl

import com.modsen.taxi.passengerservice.domain.Passenger
import com.modsen.taxi.passengerservice.dto.PassengerRequest
import com.modsen.taxi.passengerservice.dto.PassengerResponse
import com.modsen.taxi.passengerservice.error.exception.DuplicateResourceException
import com.modsen.taxi.passengerservice.error.exception.ResourceNotFoundException
import com.modsen.taxi.passengerservice.extensions.toPassenger
import com.modsen.taxi.passengerservice.extensions.toResponse
import com.modsen.taxi.passengerservice.extensions.updatePassenger
import com.modsen.taxi.passengerservice.repository.PassengerRepository
import com.modsen.taxi.passengerservice.service.PassengerService
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import java.util.*

@Service
open class PassengerServiceImpl(
    private val passengerRepository: PassengerRepository,
    private val jdbcScheduler: Scheduler
) : PassengerService {

    override fun createPassenger(passengerRequest: PassengerRequest): Mono<PassengerResponse> {
        return Mono.fromCallable {
            if (passengerRepository.existsByEmail(passengerRequest.email)) {
                throw DuplicateResourceException("Passenger with email ${passengerRequest.email} already exists.")
            }
            val passenger = passengerRequest.toPassenger().apply {
                isDeleted = false
            }
            passengerRepository.save(passenger)
        }
            .subscribeOn(jdbcScheduler)
            .map(Passenger::toResponse)
    }

    override fun updatePassenger(id: Long, passengerRequest: PassengerRequest): Mono<PassengerResponse> {
        return Mono.fromCallable {
            val passenger = passengerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow { ResourceNotFoundException("Passenger with id $id not found.") }
            passengerRequest.updatePassenger(passenger!!)
            passengerRepository.save(passenger)
        }
            .subscribeOn(jdbcScheduler)
            .map(Passenger::toResponse)
    }

    override fun getPassengerById(id: Long): Mono<PassengerResponse> {
        return Mono.fromCallable {
            passengerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow { ResourceNotFoundException("Passenger with id $id not found.") }
        }
            .subscribeOn(jdbcScheduler)
            .mapNotNull { passenger -> passenger?.toResponse() }
    }

    override fun getAllPassengers(
        pageable: Pageable,
        firstName: String?,
        lastName: String?,
        email: String?,
        isActive: Boolean?
    ): Mono<Page<PassengerResponse>> {
        return Mono.fromCallable {
            val passengerProbe = Passenger(
                firstName = firstName ?: "",
                lastName = lastName ?: "",
                email = email ?: "",
                phone = "",
                isDeleted = !(isActive ?: true)
            )
            val matcher = ExampleMatcher.matchingAll()
                .withIgnorePaths("phone")
                .withMatcher("firstName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("lastName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("email", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withTransformer("firstName", ::checkNullOrEmpty)
                .withTransformer("lastName", ::checkNullOrEmpty)
                .withTransformer("email", ::checkNullOrEmpty)
            val example = Example.of(passengerProbe, matcher)

            val passengersPage = passengerRepository.findAll(example, pageable)

            passengersPage.map{ passenger -> passenger!!.toResponse() }
        }.subscribeOn(jdbcScheduler)
    }



    override fun deletePassenger(id: Long): Mono<Void> {
        return Mono.fromCallable {
            val passenger = passengerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow { ResourceNotFoundException("Passenger with id $id not found.") }
            passenger.isDeleted = true
            passengerRepository.save(passenger)
        }
            .subscribeOn(jdbcScheduler)
            .then()
    }
    private fun checkNullOrEmpty(value: Optional<Any>): Optional<Any> {
        return if (value.isPresent && (value.get() as? String)?.isNotEmpty() == true) {
            value
        } else {
            Optional.empty()
        }
    }
}
