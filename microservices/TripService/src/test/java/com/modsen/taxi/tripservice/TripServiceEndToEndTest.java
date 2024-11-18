package com.modsen.taxi.tripservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.taxi.tripservice.domain.TripStatus;
import com.modsen.taxi.tripservice.dto.request.TripRequest;
import com.modsen.taxi.tripservice.dto.response.TripResponse;
import com.modsen.taxi.tripservice.error.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {"rating-topic"})
@Testcontainers
@AutoConfigureMockMvc
public class TripServiceEndToEndTest {


    static Network network = Network.newNetwork();
    private static PostgreSQLContainer<?> postgres;
    private static GenericContainer<?> passengerService;
    private static GenericContainer<?> driverService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        postgres = new PostgreSQLContainer<>("postgres:16")
                .withUsername("username")
                .withPassword("password")
                .withNetwork(network)
                .withNetworkAliases("postgres-db");
        postgres.start();

        try (Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword())) {

            boolean passengerDbExists = databaseExists(connection, "passenger_db");
            System.out.println("Passenger DB exists: " + passengerDbExists);

            boolean driverDbExists = databaseExists(connection, "driver_db");
            System.out.println("Driver DB exists: " + driverDbExists);

            if (!passengerDbExists) {
                connection.createStatement().executeUpdate("CREATE DATABASE passenger_db");
                System.out.println("Passenger DB created successfully.");
            }

            if (!driverDbExists) {
                connection.createStatement().executeUpdate("CREATE DATABASE driver_db");
                System.out.println("Driver DB created successfully.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check or create databases", e);
        }

        passengerService = new GenericContainer<>(DockerImageName.parse("taxiapp-passenger"))
                .withExposedPorts(8080)
                .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres-db:5432/passenger_db")
                .withEnv("SPRING_PROFILES_ACTIVE", "docker")
                .withNetwork(network)
                .waitingFor(Wait.forListeningPort());

        driverService = new GenericContainer<>(DockerImageName.parse("taxiapp-driver"))
                .withExposedPorts(8080)
                .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres-db:5432/driver_db")
                .withEnv("SPRING_PROFILES_ACTIVE", "docker")
                .withNetwork(network)
                .waitingFor(Wait.forListeningPort());

        passengerService.start();
        driverService.start();
    }

    private static boolean databaseExists(Connection connection, String dbName) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'")) {
            return resultSet.next();
        }
    }


    @AfterAll
    static void tearDown() {
        driverService.stop();
        passengerService.stop();
        postgres.stop();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        String passengerUrl = "http://localhost:" + passengerService.getMappedPort(8080) + "/api/v1/passengers/";
        String driverUrl = "http://localhost:" + driverService.getMappedPort(8080) + "/api/v1/drivers/";
        registry.add("tripservice.urls.passenger", () -> passengerUrl);
        registry.add("tripservice.urls.driver", () -> driverUrl);
    }

    @Test
    void createTrip_ShouldSuccessfullyCreateTrip_WhenDriverAndPassengerExist() throws Exception {

        TripRequest tripRequest = new TripRequest(1L, 1L, "123 Origin St", "456 Destination Ave", TripStatus.CREATED.name(), LocalDateTime.now(), BigDecimal.valueOf(50.0));
        String response = mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        TripResponse tripResponse = objectMapper.readValue(response, TripResponse.class);
        assertThat(tripResponse).isNotNull();
        assertThat(tripResponse.passengerId()).isEqualTo(1L);
        assertThat(tripResponse.driverId()).isEqualTo(1L);
        assertThat(tripResponse.originAddress()).isEqualTo("123 Origin St");
        assertThat(tripResponse.destinationAddress()).isEqualTo("456 Destination Ave");
        assertThat(tripResponse.price()).isEqualTo(BigDecimal.valueOf(50.0));
    }

    @Test
    void createTrip_ShouldReturnNotFound_WhenPassengerDoesNotExist() throws Exception {
        TripRequest tripRequest = new TripRequest(1L, 999L, "123 Origin St", "456 Destination Ave", TripStatus.CREATED.name(), LocalDateTime.now(), BigDecimal.valueOf(50.0));

        mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("Passenger with id 999 not found"));
    }

    @Test
    void createTrip_ShouldReturnNotFound_WhenDriverDoesNotExist() throws Exception {
        TripRequest tripRequest = new TripRequest(999L, 1L, "123 Origin St", "456 Destination Ave", TripStatus.CREATED.name(), LocalDateTime.now(), BigDecimal.valueOf(50.0));

        mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("Driver with id 999 not found"));
    }

}
