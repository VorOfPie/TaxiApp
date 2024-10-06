package com.modsen.taxi.passengerservice;

import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;
import com.modsen.taxi.passengerservice.repository.PassengerRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PassengerServiceIntegrationTest {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private WebTestClient client;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setupDb() {
        passengerRepository.deleteAll();
    }


    @Test
    void createPassenger_ShouldReturnPassengerResponse_WhenPassengerCreatedSuccessfully() {
        PassengerRequest passengerRequest = new PassengerRequest("John", "Doe", "john.doe@example.com", "+1234567890");

        PassengerResponse createdPassenger = postPassenger(passengerRequest);

        assertThat(createdPassenger).isNotNull();
        assertThat(createdPassenger.firstName()).isEqualTo("John");
        assertThat(createdPassenger.lastName()).isEqualTo("Doe");
        assertThat(createdPassenger.email()).isEqualTo("john.doe@example.com");
        assertThat(createdPassenger.phone()).isEqualTo("+1234567890");
    }

    @Test
    void createPassenger_ShouldReturnConflict_WhenDuplicateEmailIsProvided() {
        PassengerRequest passengerRequest = new PassengerRequest("John", "Doe", "john.doe@example.com", "+1234567890");
        postPassenger(passengerRequest);

        client.post()
                .uri("/api/v1/passengers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(passengerRequest)
                .exchange()
                .expectStatus().is4xxClientError();
    }


    @Test
    void getPassengerById_ShouldReturnPassengerResponse_WhenPassengerExists() {
        PassengerRequest passengerRequest = new PassengerRequest("John", "Doe", "john.doe@example.com", "+1234567890");
        PassengerResponse createdPassenger = postPassenger(passengerRequest);

        assert createdPassenger != null;
        client.get()
                .uri("/api/v1/passengers/{id}", createdPassenger.id())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PassengerResponse.class)
                .consumeWith(response -> {
                    PassengerResponse passengerResponse = response.getResponseBody();
                    assertThat(passengerResponse).isNotNull();
                    assertThat(passengerResponse.firstName()).isEqualTo("John");
                    assertThat(passengerResponse.lastName()).isEqualTo("Doe");
                    assertThat(passengerResponse.email()).isEqualTo("john.doe@example.com");
                    assertThat(passengerResponse.phone()).isEqualTo("+1234567890");
                });
    }


    @Test
    void getPassengerById_ShouldReturnNotFound_WhenPassengerDoesNotExist() {
        client.get()
                .uri("/api/v1/passengers/{id}", 9999)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updatePassenger_ShouldReturnUpdatedPassengerResponse_WhenPassengerExists() {
        PassengerRequest passengerRequest = new PassengerRequest("John", "Doe", "john.doe@example.com", "+1234567890");
        PassengerResponse createdPassenger = postPassenger(passengerRequest);

        PassengerRequest updatedPassengerRequest = new PassengerRequest("Johnathan", "Doe", "john.doe@example.com", "+0987654321");

        PassengerResponse updatedPassenger = client.put()
                .uri("/api/v1/passengers/{id}", createdPassenger.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedPassengerRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PassengerResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(updatedPassenger).isNotNull();
        assertThat(updatedPassenger.firstName()).isEqualTo("Johnathan");
        assertThat(updatedPassenger.lastName()).isEqualTo("Doe");
        assertThat(updatedPassenger.email()).isEqualTo("john.doe@example.com");
        assertThat(updatedPassenger.phone()).isEqualTo("+0987654321");
    }

    @Test
    void updatePassenger_ShouldReturnNotFound_WhenPassengerDoesNotExist() {
        PassengerRequest updatedPassengerRequest = new PassengerRequest("John", "Doe", "john.doe@example.com", "+1234567890");

        client.put()
                .uri("/api/v1/passengers/{id}", 9999)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedPassengerRequest)
                .exchange()
                .expectStatus().isNotFound();
    }


    @Test
    void deletePassenger_ShouldMarkPassengerAsDeleted_WhenPassengerExists() {
        PassengerRequest passengerRequest = new PassengerRequest("John", "Doe", "john.doe@example.com", "+1234567890");
        PassengerResponse createdPassenger = postPassenger(passengerRequest);

        assert createdPassenger != null;
        client.delete()
                .uri("/api/v1/passengers/{id}", createdPassenger.id())
                .exchange()
                .expectStatus().isNoContent();

        client.get()
                .uri("/api/v1/passengers/{id}", createdPassenger.id())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deletePassenger_ShouldReturnNotFound_WhenPassengerDoesNotExist() {
        client.delete()
                .uri("/api/v1/passengers/{id}", 9999)
                .exchange()
                .expectStatus().isNotFound();
    }


    @Test
    void getPassengers_ShouldReturnFilteredResponse_WhenFilterByFirstName() {
        postPassenger(new PassengerRequest("Alice", "Smith", "alice.smith@example.com", "+1234567891"));
        postPassenger(new PassengerRequest("Bob", "Johnson", "bob.johnson@example.com", "+1234567892"));
        postPassenger(new PassengerRequest("Alice", "Brown", "alice.brown@example.com", "+1234567893"));

        client.get()
                .uri("/api/v1/passengers?firstName=Alice")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .consumeWith(response -> {
                    Map<String, Object> responseBody = response.getResponseBody();
                    List<LinkedHashMap<String, Object>> passengersMap =
                            (List<LinkedHashMap<String, Object>>) responseBody.get("passengers");

                    List<PassengerResponse> passengers = passengersMap.stream()
                            .map(map -> new PassengerResponse(
                                    ((Number) map.get("id")).longValue(),
                                    (String) map.get("firstName"),
                                    (String) map.get("lastName"),
                                    (String) map.get("email"),
                                    (String) map.get("phone")
                            ))
                            .collect(Collectors.toList());

                    assertThat(passengers).hasSize(2);
                    assertThat(passengers)
                            .extracting(PassengerResponse::firstName)
                            .containsExactlyInAnyOrder("Alice", "Alice");

                    assertThat(responseBody.get("currentPage")).isEqualTo(0);
                    assertThat(responseBody.get("totalItems")).isEqualTo(2);
                    assertThat(responseBody.get("totalPages")).isEqualTo(1);
                });
    }


    private PassengerResponse postPassenger(PassengerRequest passengerRequest) {
        return client.post()
                .uri("/api/v1/passengers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(passengerRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PassengerResponse.class)
                .returnResult()
                .getResponseBody();
    }
}
