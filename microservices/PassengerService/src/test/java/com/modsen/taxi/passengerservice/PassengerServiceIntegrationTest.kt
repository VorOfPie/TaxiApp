package com.modsen.taxi.passengerservice

import com.modsen.taxi.passengerservice.dto.PassengerRequest
import com.modsen.taxi.passengerservice.dto.PassengerResponse
import com.modsen.taxi.passengerservice.repository.PassengerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PassengerServiceIntegrationTest {

    companion object {
        val postgres = PostgreSQLContainer<Nothing>("postgres:16")

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            postgres.start()
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            postgres.stop()
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Autowired
    private lateinit var passengerRepository: PassengerRepository

    @Autowired
    private lateinit var client: WebTestClient

    @BeforeEach
    fun setupDb() {
        passengerRepository.deleteAll()
    }

    @Test
    fun `createPassenger should return PassengerResponse when passenger created successfully`() {
        val passengerRequest = PassengerRequest("John", "Doe", "john.doe@example.com", "+1234567890")

        val createdPassenger = postPassenger(passengerRequest)

        assertThat(createdPassenger).isNotNull
        assertThat(createdPassenger?.firstName).isEqualTo("John")
        assertThat(createdPassenger?.lastName).isEqualTo("Doe")
        assertThat(createdPassenger?.email).isEqualTo("john.doe@example.com")
        assertThat(createdPassenger?.phone).isEqualTo("+1234567890")
    }

    @Test
    fun `createPassenger should return conflict when duplicate email is provided`() {
        val passengerRequest = PassengerRequest("John", "Doe", "john.doe@example.com", "+1234567890")
        postPassenger(passengerRequest)

        client.post()
            .uri("/api/v1/passengers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(passengerRequest)
            .exchange()
            .expectStatus().is4xxClientError
    }

    @Test
    fun `getPassengerById should return PassengerResponse when passenger exists`() {
        val passengerRequest = PassengerRequest("John", "Doe", "john.doe@example.com", "+1234567890")
        val createdPassenger = postPassenger(passengerRequest)

        assertThat(createdPassenger).isNotNull
        client.get()
            .uri("/api/v1/passengers/{id}", createdPassenger?.id)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(PassengerResponse::class.java)
            .consumeWith { response ->
                val passengerResponse = response.responseBody
                assertThat(passengerResponse).isNotNull
                assertThat(passengerResponse?.firstName).isEqualTo("John")
                assertThat(passengerResponse?.lastName).isEqualTo("Doe")
                assertThat(passengerResponse?.email).isEqualTo("john.doe@example.com")
                assertThat(passengerResponse?.phone).isEqualTo("+1234567890")
            }
    }

    @Test
    fun `getPassengerById should return not found when passenger does not exist`() {
        client.get()
            .uri("/api/v1/passengers/{id}", 9999)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `updatePassenger should return updated PassengerResponse when passenger exists`() {
        val passengerRequest = PassengerRequest("John", "Doe", "john.doe@example.com", "+1234567890")
        val createdPassenger = postPassenger(passengerRequest)

        val updatedPassengerRequest = PassengerRequest("Johnathan", "Doe", "john.doe@example.com", "+0987654321")

        val updatedPassenger = client.put()
            .uri("/api/v1/passengers/{id}", createdPassenger?.id)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updatedPassengerRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody(PassengerResponse::class.java)
            .returnResult()
            .responseBody

        assertThat(updatedPassenger).isNotNull
        assertThat(updatedPassenger?.firstName).isEqualTo("Johnathan")
        assertThat(updatedPassenger?.lastName).isEqualTo("Doe")
        assertThat(updatedPassenger?.email).isEqualTo("john.doe@example.com")
        assertThat(updatedPassenger?.phone).isEqualTo("+0987654321")
    }

    @Test
    fun `updatePassenger should return not found when passenger does not exist`() {
        val updatedPassengerRequest = PassengerRequest("John", "Doe", "john.doe@example.com", "+1234567890")

        client.put()
            .uri("/api/v1/passengers/{id}", 9999)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updatedPassengerRequest)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `deletePassenger should mark passenger as deleted when passenger exists`() {
        val passengerRequest = PassengerRequest("John", "Doe", "john.doe@example.com", "+1234567890")
        val createdPassenger = postPassenger(passengerRequest)

        assertThat(createdPassenger).isNotNull
        client.delete()
            .uri("/api/v1/passengers/{id}", createdPassenger?.id)
            .exchange()
            .expectStatus().isNoContent

        client.get()
            .uri("/api/v1/passengers/{id}", createdPassenger?.id)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `deletePassenger should return not found when passenger does not exist`() {
        client.delete()
            .uri("/api/v1/passengers/{id}", 9999)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `getPassengers should return filtered response when filter by firstName`() {
        postPassenger(PassengerRequest("Alice", "Smith", "alice.smith@example.com", "+1234567891"))
        postPassenger(PassengerRequest("Bob", "Johnson", "bob.johnson@example.com", "+1234567892"))
        postPassenger(PassengerRequest("Alice", "Brown", "alice.brown@example.com", "+1234567893"))

        client.get()
            .uri { uriBuilder -> uriBuilder.path("/api/v1/passengers").queryParam("firstName", "Alice").build() }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .consumeWith { response ->
                val responseBody = response.responseBody as Map<String, Any>
                val passengers = (responseBody["passengers"] as List<Map<String, Any>>).map {
                    PassengerResponse(
                        id = (it["id"] as Number).toLong(),
                        firstName = it["firstName"] as String,
                        lastName = it["lastName"] as String,
                        email = it["email"] as String,
                        phone = it["phone"] as String
                    )
                }

                assertThat(passengers).hasSize(2)
                assertThat(passengers.map { it.firstName }).containsExactlyInAnyOrder("Alice", "Alice")
                assertThat(responseBody["currentPage"]).isEqualTo(0)
                assertThat(responseBody["totalItems"]).isEqualTo(2)
                assertThat(responseBody["totalPages"]).isEqualTo(1)
            }
    }

    private fun postPassenger(passengerRequest: PassengerRequest): PassengerResponse? {
        return client.post()
            .uri("/api/v1/passengers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(passengerRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(PassengerResponse::class.java)
            .returnResult()
            .responseBody
    }
}
