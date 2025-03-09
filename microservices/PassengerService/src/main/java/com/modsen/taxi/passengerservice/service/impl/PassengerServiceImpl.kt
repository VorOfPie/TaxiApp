package com.modsen.taxi.passengerservice.service.impl

import com.modsen.taxi.passengerservice.domain.Passenger
import com.modsen.taxi.passengerservice.dto.PassengerRequest
import com.modsen.taxi.passengerservice.dto.PassengerResponse
import com.modsen.taxi.passengerservice.error.exception.AccessDeniedException
import com.modsen.taxi.passengerservice.error.exception.DuplicateResourceException
import com.modsen.taxi.passengerservice.error.exception.ResourceNotFoundException
import com.modsen.taxi.passengerservice.extensions.toPassenger
import com.modsen.taxi.passengerservice.extensions.toResponse
import com.modsen.taxi.passengerservice.extensions.updatePassenger
import com.modsen.taxi.passengerservice.repository.PassengerRepository
import com.modsen.taxi.passengerservice.service.PassengerService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import java.util.*

@Service
class PassengerServiceImpl(
    private val passengerRepository: PassengerRepository,
    private val jdbcScheduler: Scheduler
) : PassengerService {

    private val log: Logger = LoggerFactory.getLogger(PassengerServiceImpl::class.java)

    override fun getPassengerById(id: Long, principalEmail: String, isAdmin: Boolean): Mono<PassengerResponse> {
        log.info("Entering getPassengerById with id: $id, principalEmail: $principalEmail, isAdmin: $isAdmin")

        return Mono.fromCallable {
            log.debug("Fetching passenger with id $id from repository")
            val passenger = passengerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow { ResourceNotFoundException("Passenger with id $id not found.") }

            if (!isAdmin && passenger.email != principalEmail) {
                log.warn("Access denied for user with email $principalEmail to passenger with id $id")
                throw AccessDeniedException("You do not have permission to access this passenger's information.")
            }

            passenger
        }
            .subscribeOn(jdbcScheduler)
            .map { it.toResponse() }
            .doOnSuccess {
                log.info("Successfully fetched passenger with id $id")
            }
            .doOnError {
                log.error("Error fetching passenger with id $id: ${it.message}", it)
            }
    }

    override fun createPassenger(passengerRequest: PassengerRequest): Mono<PassengerResponse> {
        log.info("Entering createPassenger with email: ${passengerRequest.email}")

        return Mono.fromCallable {
            log.debug("Checking if passenger with email ${passengerRequest.email} exists")
            if (passengerRepository.existsByEmail(passengerRequest.email)) {
                log.warn("Passenger with email ${passengerRequest.email} already exists")
                throw DuplicateResourceException("Passenger with email ${passengerRequest.email} already exists.")
            }

            val passenger = passengerRequest.toPassenger().apply {
                isDeleted = false
            }

            log.debug("Saving new passenger with email ${passengerRequest.email}")
            passengerRepository.save(passenger)
        }
            .subscribeOn(jdbcScheduler)
            .map { it.toResponse() }
            .doOnSuccess {
                log.info("Successfully created passenger with email ${passengerRequest.email}")
            }
            .doOnError {
                log.error("Error creating passenger with email ${passengerRequest.email}: ${it.message}", it)
            }
    }

    override fun updatePassenger(id: Long, passengerUpdateRequest: PassengerRequest, principalEmail: String, isAdmin: Boolean): Mono<PassengerResponse> {
        log.info("Entering updatePassenger with id: $id, principalEmail: $principalEmail, isAdmin: $isAdmin")

        return Mono.fromCallable {
            log.debug("Fetching passenger with id $id for update")
            val passenger = passengerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow { ResourceNotFoundException("Passenger with id $id not found.") }

            if (!isAdmin && passenger.email != principalEmail) {
                log.warn("Access denied for user with email $principalEmail to update passenger with id $id")
                throw AccessDeniedException("You do not have permission to update this passenger's information.")
            }

            passengerUpdateRequest.updatePassenger(passenger)
            log.debug("Saving updated passenger with id $id")
            passengerRepository.save(passenger)
        }
            .subscribeOn(jdbcScheduler)
            .map { it.toResponse() }
            .doOnSuccess {
                log.info("Successfully updated passenger with id $id")
            }
            .doOnError {
                log.error("Error updating passenger with id $id: ${it.message}", it)
            }
    }

    override fun deletePassenger(id: Long, principalEmail: String, isAdmin: Boolean): Mono<Void> {
        log.info("Entering deletePassenger with id: $id, principalEmail: $principalEmail, isAdmin: $isAdmin")

        return Mono.fromCallable {
            log.debug("Fetching passenger with id $id for deletion")
            val passenger = passengerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow { ResourceNotFoundException("Passenger with id $id not found.") }

            if (!isAdmin && passenger.email != principalEmail) {
                log.warn("Access denied for user with email $principalEmail to delete passenger with id $id")
                throw AccessDeniedException("You do not have permission to delete this passenger.")
            }

            log.debug("Marking passenger with id $id as deleted")
            passenger.isDeleted = true
            passengerRepository.save(passenger)
        }
            .subscribeOn(jdbcScheduler)
            .doOnSuccess {
                log.info("Successfully deleted passenger with id $id")
            }
            .doOnError {
                log.error("Error deleting passenger with id $id: ${it.message}", it)
            }
            .then()
    }

    override fun getAllPassengers(pageable: Pageable, firstName: String?, lastName: String?, email: String?, isActive: Boolean): Mono<Page<PassengerResponse>> {
        log.info("Entering getAllPassengers with filters - firstName: $firstName, lastName: $lastName, email: $email, isActive: $isActive")

        return Mono.fromCallable {
            log.debug("Building search example for passengers")
            val passengerProbe = Passenger(
                firstName = firstName ?: "",
                lastName = lastName ?: "",
                email = email ?: "",
                phone = "",
                isDeleted = !(isActive)
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

            log.debug("Fetching passengers from repository with filters")
            passengerRepository.findAll(example, pageable)
        }
            .subscribeOn(jdbcScheduler)
            .map { it.map { passenger -> passenger.toResponse() } }
            .doOnSuccess {
                log.info("Successfully fetched passengers")
            }
            .doOnError {
                log.error("Error fetching passengers: ${it.message}", it)
            }
    }
    private fun checkNullOrEmpty(value: Optional<Any>): Optional<Any> {
        return if (value.isPresent && (value.get() as? String)?.isNotEmpty() == true) {
            value
        } else {
            Optional.empty()
        }
    }
}
