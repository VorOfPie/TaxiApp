package com.modsen.taxi.driversrvice.car;

import com.modsen.taxi.driversrvice.dto.request.CreateCarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import com.modsen.taxi.driversrvice.repository.CarRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
public class CarServiceIntegrationTest {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private CarRepository carRepository;

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
        carRepository.deleteAll();
    }

    @Test
    void createCar_ShouldReturnCarResponse_WhenCarCreatedSuccessfully() {
        CreateCarRequest carRequest = new CreateCarRequest("Toyota", "White", "ABC123");

        CarResponse createdCar = postCar(carRequest);

        assertThat(createdCar).isNotNull();
        assertThat(createdCar.brand()).isEqualTo("Toyota");
        assertThat(createdCar.color()).isEqualTo("White");
        assertThat(createdCar.licensePlate()).isEqualTo("ABC123");
    }

    @Test
    void createCar_ShouldReturnConflict_WhenDuplicateLicensePlateIsProvided() {
        CreateCarRequest carRequest = new CreateCarRequest("Toyota", "White", "ABC123");
        postCar(carRequest);

        client.post()
                .uri("/api/v1/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(carRequest)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void getCarById_ShouldReturnCarResponse_WhenCarExists() {
        CreateCarRequest carRequest = new CreateCarRequest("Toyota", "White", "ABC123");
        CarResponse createdCar = postCar(carRequest);

        assertThat(createdCar).isNotNull();

        client.get()
                .uri("/api/v1/cars/{id}", createdCar.id())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CarResponse.class)
                .consumeWith(response -> {
                    CarResponse carResponse = response.getResponseBody();
                    assertThat(carResponse).isNotNull();
                    assertThat(carResponse.brand()).isEqualTo("Toyota");
                    assertThat(carResponse.color()).isEqualTo("White");
                    assertThat(carResponse.licensePlate()).isEqualTo("ABC123");
                });
    }

    @Test
    void getCarById_ShouldReturnNotFound_WhenCarDoesNotExist() {
        client.get()
                .uri("/api/v1/cars/{id}", 9999)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateCar_ShouldReturnUpdatedCarResponse_WhenCarExists() {
        CreateCarRequest carRequest = new CreateCarRequest("Toyota", "White", "ABC123");
        CarResponse createdCar = postCar(carRequest);

        CreateCarRequest updatedCarRequest = new CreateCarRequest("Honda", "Black", "DEF456");

        CarResponse updatedCar = client.put()
                .uri("/api/v1/cars/{id}", createdCar.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedCarRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CarResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(updatedCar).isNotNull();
        assertThat(updatedCar.brand()).isEqualTo("Honda");
        assertThat(updatedCar.color()).isEqualTo("Black");
        assertThat(updatedCar.licensePlate()).isEqualTo("DEF456");
    }

    @Test
    void updateCar_ShouldReturnNotFound_WhenCarDoesNotExist() {
        CreateCarRequest updatedCarRequest = new CreateCarRequest("Toyota", "White", "ABC123");

        client.put()
                .uri("/api/v1/cars/{id}", 9999)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedCarRequest)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteCar_ShouldMarkCarAsDeleted_WhenCarExists() {
        CreateCarRequest carRequest = new CreateCarRequest("Toyota", "White", "ABC123");
        CarResponse createdCar = postCar(carRequest);

        assert createdCar != null;
        client.delete()
                .uri("/api/v1/cars/{id}", createdCar.id())
                .exchange()
                .expectStatus().isNoContent();

        client.get()
                .uri("/api/v1/cars/{id}", createdCar.id())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteCar_ShouldReturnNotFound_WhenCarDoesNotExist() {
        client.delete()
                .uri("/api/v1/cars/{id}", 9999)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getCars_ShouldReturnFilteredResponse_WhenFilterByBrand() {
        postCar(new CreateCarRequest("Toyota", "White", "ABC123"));
        postCar(new CreateCarRequest("Honda", "Black", "DEF456"));
        postCar(new CreateCarRequest("Toyota", "Red", "GHI789"));

        client.get()
                .uri("/api/v1/cars?brand=Toyota")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .consumeWith(response -> {
                    Map<String, Object> responseBody = response.getResponseBody();
                    List<LinkedHashMap<String, Object>> carsMap =
                            (List<LinkedHashMap<String, Object>>) responseBody.get("cars");

                    List<CarResponse> cars = carsMap.stream()
                            .map(map -> new CarResponse(
                                    ((Number) map.get("id")).longValue(),
                                    (String) map.get("brand"),
                                    (String) map.get("color"),
                                    (String) map.get("licensePlate")
                            ))
                            .collect(Collectors.toList());

                    assertThat(cars).hasSize(2);
                    assertThat(cars)
                            .extracting(CarResponse::brand)
                            .containsExactlyInAnyOrder("Toyota", "Toyota");

                    assertThat(responseBody.get("currentPage")).isEqualTo(0);
                    assertThat(responseBody.get("totalItems")).isEqualTo(2);
                    assertThat(responseBody.get("totalPages")).isEqualTo(1);
                });
    }

    private CarResponse postCar(CreateCarRequest carRequest) {
        return client.post()
                .uri("/api/v1/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(carRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CarResponse.class)
                .returnResult()
                .getResponseBody();
    }
}
