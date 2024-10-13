package com.modsen.taxi.driversrvice.driver;

import com.modsen.taxi.driversrvice.dto.request.CarRequest;
import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
import com.modsen.taxi.driversrvice.repository.CarRepository;
import com.modsen.taxi.driversrvice.repository.DriverRepository;
import org.junit.Assert;
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
public class DriverServiceIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private DriverRepository driverRepository;
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
        driverRepository.deleteAll();
    }

    @Test
    void createDriver_ShouldReturnDriverResponse_WhenDriverCreatedSuccessfully() {
        List<CarRequest> cars = List.of(new CarRequest(null, "Toyota", "Camry", "ABC123"));

        DriverResponse createdDriver = postDriver(new DriverRequest("John", "Doe", "+1234567890", "Male", cars));

        assertThat(createdDriver).isNotNull();
        assertThat(createdDriver.firstName()).isEqualTo("John");
        assertThat(createdDriver.lastName()).isEqualTo("Doe");
        assertThat(createdDriver.phone()).isEqualTo("+1234567890");
        assertThat(createdDriver.gender()).isEqualTo("Male");
        assertThat(createdDriver.cars()).hasSize(1);
        assertThat(createdDriver.cars().get(0).licensePlate()).isEqualTo("ABC123");
    }

    @Test
    void createDriver_ShouldReturnBadRequest_WhenInvalidPhoneNumberProvided() {
        DriverRequest driverRequest = new DriverRequest("John", "Doe", "invalid-phone", "Male", List.of());

        client.post()
                .uri("/api/v1/drivers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(driverRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getDriverById_ShouldReturnDriverResponse_WhenDriverExists() {
        List<CarRequest> cars = List.of(new CarRequest(null, "Toyota", "Camry", "ABC123"));

        DriverResponse createdDriver = postDriver(new DriverRequest("John", "Doe", "+1234567890", "Male", cars));

        assertThat(createdDriver).isNotNull();
        client.get()
                .uri("/api/v1/drivers/{id}", createdDriver.id())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DriverResponse.class)
                .consumeWith(response -> {
                    DriverResponse driverResponse = response.getResponseBody();
                    assertThat(driverResponse).isNotNull();
                    assertThat(driverResponse.firstName()).isEqualTo("John");
                    assertThat(driverResponse.lastName()).isEqualTo("Doe");
                    assertThat(driverResponse.phone()).isEqualTo("+1234567890");
                    assertThat(driverResponse.gender()).isEqualTo("Male");
                    assertThat(driverResponse.cars()).hasSize(1);
                    assertThat(driverResponse.cars().get(0).licensePlate()).isEqualTo("ABC123");
                });
    }

    @Test
    void getDriverById_ShouldReturnNotFound_WhenDriverDoesNotExist() {
        client.get()
                .uri("/api/v1/drivers/{id}", 9999)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateDriver_ShouldReturnUpdatedDriverResponse_WhenDriverExists() {
        List<CarRequest> cars = List.of(new CarRequest(null, "Toyota", "Camry", "ABC123"));

        DriverResponse createdDriver = postDriver(new DriverRequest("John", "Doe", "+1234567890", "Male", cars));

        List<CarRequest> updatedCars = List.of(new CarRequest(null, "Honda", "Accord", "DEF456"));
        DriverRequest updatedDriverRequest = new DriverRequest("Jane", "Doe", "+9876543210", "Female", updatedCars);

        DriverResponse updatedDriver = client.put()
                .uri("/api/v1/drivers/{id}", createdDriver.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedDriverRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DriverResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(updatedDriver).isNotNull();
        assertThat(updatedDriver.firstName()).isEqualTo("Jane");
        assertThat(updatedDriver.lastName()).isEqualTo("Doe");
        assertThat(updatedDriver.phone()).isEqualTo("+9876543210");
        assertThat(updatedDriver.gender()).isEqualTo("Female");
        assertThat(updatedDriver.cars()).hasSize(1);
        assertThat(updatedDriver.cars().get(0).licensePlate()).isEqualTo("DEF456");
    }

    @Test
    void deleteDriver_ShouldMarkDriverAsDeleted_WhenDriverExists() {
        List<CarRequest> cars = List.of(new CarRequest(null, "Toyota", "Camry", "ABC123"));

        DriverResponse createdDriver = postDriver(new DriverRequest("John", "Doe", "+1234567890", "Male", cars));

        assert createdDriver != null;
        client.delete()
                .uri("/api/v1/drivers/{id}", createdDriver.id())
                .exchange()
                .expectStatus().isNoContent();

        client.get()
                .uri("/api/v1/drivers/{id}", createdDriver.id())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAllDrivers_ShouldReturnPagedDrivers_WhenDriversExist() {
        List<CarRequest> cars1 = List.of(new CarRequest(null, "Toyota", "Camry", "ABC123"));
        List<CarRequest> cars2 = List.of(new CarRequest(null, "NotToyota", "NotCamry", "CBA123"));
        List<CarRequest> cars3 = List.of(new CarRequest(null, "Toyota", "Camry", "ACB123"));
        DriverRequest driver1 = new DriverRequest("John", "Doe", "+1234567890", "Male", cars1);
        DriverRequest driver2 = new DriverRequest("Jane", "Doe", "+7876543210", "Female", cars2);
        DriverRequest driver3 = new DriverRequest("Jack", "Smith", "+1122334455", "Male", cars3);

        postDriver(driver1);
        postDriver(driver2);
        postDriver(driver3);

        client.get()
                .uri("/api/v1/drivers?lastName=Doe")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .consumeWith(response -> {
                    Map<String, Object> responseBody = response.getResponseBody();
                    List<LinkedHashMap<String, Object>> driversMap =
                            (List<LinkedHashMap<String, Object>>) responseBody.get("drivers");

                    List<DriverResponse> drivers = driversMap.stream()
                            .map(map -> new DriverResponse(
                                    ((Number) map.get("id")).longValue(),
                                    (String) map.get("firstName"),
                                    (String) map.get("lastName"),
                                    (String) map.get("phone"),
                                    (String) map.get("gender"),
                                    (List<CarResponse>) map.get("cars")
                            ))
                            .collect(Collectors.toList());

                    assertThat(drivers).hasSize(2);
                    assertThat(drivers)
                            .extracting(DriverResponse::lastName)
                            .containsExactlyInAnyOrder("Doe", "Doe");

                    assertThat(responseBody.get("currentPage")).isEqualTo(0);
                    assertThat(responseBody.get("totalItems")).isEqualTo(2);
                    assertThat(responseBody.get("totalPages")).isEqualTo(1);
                });
    }

    private DriverResponse postDriver(DriverRequest driverRequest) {
        return client.post()
                .uri("/api/v1/drivers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(driverRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DriverResponse.class)
                .returnResult()
                .getResponseBody();
    }

}