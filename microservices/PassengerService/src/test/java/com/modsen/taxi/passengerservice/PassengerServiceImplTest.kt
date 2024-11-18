package com.modsen.taxi.passengerservice

import com.modsen.taxi.passengerservice.domain.Passenger
import com.modsen.taxi.passengerservice.dto.PassengerRequest
import com.modsen.taxi.passengerservice.dto.PassengerResponse
import com.modsen.taxi.passengerservice.error.exception.DuplicateResourceException
import com.modsen.taxi.passengerservice.error.exception.ResourceNotFoundException
import com.modsen.taxi.passengerservice.repository.PassengerRepository
import com.modsen.taxi.passengerservice.service.impl.PassengerServiceImpl
import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import org.hibernate.internal.util.collections.CollectionHelper.listOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.*
import reactor.core.Disposable
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.test.StepVerifier
import java.util.*

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PassengerServiceImplTest {

    @MockkBean
    private lateinit var passengerRepository: PassengerRepository

    @MockkBean
    private lateinit var jdbcScheduler: Scheduler

    @Autowired
    private lateinit var passengerService: PassengerServiceImpl

    @BeforeEach
    fun setUp() {
        every { jdbcScheduler.schedule(any()) } answers {
            val runnable = it.invocation.args[0] as Runnable
            runnable.run()
            mockk<Disposable> {
                every { dispose() } just Runs
            }
        }
    }

    @Test
    fun `createPassenger should return PassengerResponse when passenger is created successfully`() {
        val passengerRequest = PassengerRequest("John", "Doe", "john.doe@example.com", "123123123123")
        val passenger1 = Passenger(1L, "John", "Doe", "john.doe@example.com", "123123123123", false)

        every { passengerRepository.existsByEmail(any()) } returns false
        every { passengerRepository.save(any()) } returns passenger1

        val result: Mono<PassengerResponse> = passengerService.createPassenger(passengerRequest)

        StepVerifier.create(result)
            .expectNextMatches { it.email == "john.doe@example.com" }
            .verifyComplete()

        verify { passengerRepository.existsByEmail(any()) }
        verify { passengerRepository.save(any()) }
    }

    @Test
    fun `createPassenger should throw DuplicateResourceException when passenger already exists`() {
        val passengerRequest = PassengerRequest("John", "Doe", "john.doe@example.com", "123123123123")

        every { passengerRepository.existsByEmail(any()) } returns true

        val result: Mono<PassengerResponse> = passengerService.createPassenger(passengerRequest)

        StepVerifier.create(result)
            .expectError(DuplicateResourceException::class.java)
            .verify()
        verify { passengerRepository.existsByEmail(any()) }
        verify(exactly = 0) { passengerRepository.save(any<Passenger>()) }
    }

    @Test
    fun `getPassengerById should return PassengerResponse when passenger exists`() {
        val passenger1 = Passenger(1L, "John", "Doe", "john.doe@example.com", "123123123123", false)

        every { passengerRepository.findByIdAndIsDeletedFalse(any()) } returns Optional.of(passenger1)

        val result: Mono<PassengerResponse> = passengerService.getPassengerById(1L)

        StepVerifier.create(result)
            .expectNextMatches { it.email == "john.doe@example.com" }
            .verifyComplete()

        verify { passengerRepository.findByIdAndIsDeletedFalse(any()) }
    }

    @Test
    fun `getPassengerById should throw ResourceNotFoundException when passenger not found`() {
        every { passengerRepository.findByIdAndIsDeletedFalse(any()) } returns Optional.empty()

        val result: Mono<PassengerResponse> = passengerService.getPassengerById(1L)

        StepVerifier.create(result)
            .expectError(ResourceNotFoundException::class.java)
            .verify()

        verify { passengerRepository.findByIdAndIsDeletedFalse(any()) }
    }

    @Test
    fun `deletePassenger should mark passenger as deleted when passenger exists`() {
        val passenger1 = Passenger(1L, "John", "Doe", "john.doe@example.com", "123123123123", false)

        every { passengerRepository.findByIdAndIsDeletedFalse(any()) } returns Optional.of(passenger1)
        every { passengerRepository.save(any()) } returns passenger1

        val result: Mono<Void> = passengerService.deletePassenger(1L)

        StepVerifier.create(result)
            .verifyComplete()

        assert(passenger1.isDeleted)
        verify { passengerRepository.findByIdAndIsDeletedFalse(any()) }
        verify { passengerRepository.save(passenger1) }
    }

    @Test
    fun `deletePassenger should throw ResourceNotFoundException when passenger does not exist`() {
        every { passengerRepository.findByIdAndIsDeletedFalse(any()) } returns Optional.empty()

        val result: Mono<Void> = passengerService.deletePassenger(1L)

        StepVerifier.create(result)
            .expectError(ResourceNotFoundException::class.java)
            .verify()

        verify { passengerRepository.findByIdAndIsDeletedFalse(any()) }
        verify(exactly = 0) { passengerRepository.save(any<Passenger>()) }
    }


    @Test
    fun `getAllPassengers should return paged passengers when passengers exist`() {
        val passenger1 = Passenger(1L, "John", "Doe", "john.doe@example.com", "123123123123", false)
        val passenger2 = Passenger(2L, "Jane", "Doe", "jane.doe@example.com", "456456456456", false)
        val pageable = PageRequest.of(0, 2)
        val passengerPage = PageImpl(listOf(passenger1, passenger2), pageable, 2)
        every { passengerRepository.findAll(any<Example<Passenger>>(), any<Pageable>()) } returns passengerPage

        val result: Mono<Page<PassengerResponse>> = passengerService.getAllPassengers(pageable, null, null, null, true)

        StepVerifier.create(result)
            .expectNextMatches { page ->
                assertEquals(2, page.size)
                assertEquals(2, page.totalElements)
                val responses = page.content
                responses.size == 2 &&
                        responses[0].email == "john.doe@example.com" &&
                        responses[1].email == "jane.doe@example.com"
            }
            .verifyComplete()

        verify { passengerRepository.findAll(any<Example<Passenger>>(), any<Pageable>()) }
    }

    @Test
    fun `getAllPassengers should return filtered passengers when filters are applied`() {
        val passenger1 = Passenger(1L, "John", "Doe", "john.doe@example.com", "123123123123", false)
        val pageable = PageRequest.of(0, 2)
        val passengerPage = PageImpl(listOf(passenger1), pageable, 1)

        every { passengerRepository.findAll(any<Example<Passenger>>(), any<Pageable>()) } returns passengerPage

        val result: Mono<Page<PassengerResponse>> = passengerService.getAllPassengers(pageable, "John", "", "", true)

        StepVerifier.create(result)
            .expectNextMatches { page ->
                val passengers = page.content
                passengers.size == 1 &&
                        passengers[0].firstName == "John" &&
                        passengers[0].lastName == "Doe"
            }
            .verifyComplete()

        verify { passengerRepository.findAll(any<Example<Passenger>>(), any<Pageable>()) }
    }
}
