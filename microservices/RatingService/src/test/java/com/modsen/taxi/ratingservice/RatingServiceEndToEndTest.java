package com.modsen.taxi.ratingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.taxi.ratingservice.dto.RatingRequest;
import com.modsen.taxi.ratingservice.dto.response.RatingResponse;
import com.modsen.taxi.ratingservice.error.exception.ResourceNotFoundException;
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {"rating-topic"})
@Testcontainers
@AutoConfigureMockMvc
public class RatingServiceEndToEndTest {

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
        registry.add("ratingservice.urls.passenger", () -> passengerUrl);
        registry.add("ratingservice.urls.driver", () -> driverUrl);
    }

    @Test
    void createRating_ShouldSuccessfullyCreateRating_WhenDriverAndPassengerExist() throws Exception {
        RatingRequest ratingRequest = new RatingRequest(1L, 1L, 5.0, "Excellent service");
        String response = mockMvc.perform(post("/api/v1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        RatingResponse ratingResponse = objectMapper.readValue(response, RatingResponse.class);
        assertThat(ratingResponse).isNotNull();
        assertThat(ratingResponse.passengerId()).isEqualTo(1L);
        assertThat(ratingResponse.driverId()).isEqualTo(1L);
        assertThat(ratingResponse.score()).isEqualTo(5.0);
        assertThat(ratingResponse.comment()).isEqualTo("Excellent service");
    }

    @Test
    void createRating_ShouldReturnNotFound_WhenPassengerDoesNotExist() throws Exception {
        RatingRequest ratingRequest = new RatingRequest(1L, 999L, 5.0, "Excellent service");

        mockMvc.perform(post("/api/v1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("Passenger with id 999 not found"));
    }

    @Test
    void createRating_ShouldReturnNotFound_WhenDriverDoesNotExist() throws Exception {
        RatingRequest ratingRequest = new RatingRequest(999L, 1L, 5.0, "Excellent service");

        mockMvc.perform(post("/api/v1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("Driver with id 999 not found"));
    }
}
