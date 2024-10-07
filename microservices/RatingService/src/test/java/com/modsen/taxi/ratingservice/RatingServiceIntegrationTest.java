package com.modsen.taxi.ratingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.taxi.ratingservice.config.DriverClient;
import com.modsen.taxi.ratingservice.config.PassengerClient;
import com.modsen.taxi.ratingservice.domain.Rating;
import com.modsen.taxi.ratingservice.dto.RatingRequest;
import com.modsen.taxi.ratingservice.dto.response.DriverResponse;
import com.modsen.taxi.ratingservice.dto.response.PassengerResponse;
import com.modsen.taxi.ratingservice.dto.response.RatingResponse;
import com.modsen.taxi.ratingservice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.ratingservice.repository.RatingRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@EnableKafka
@EmbeddedKafka(partitions = 1, topics = {"rating-topic"}, brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092"})
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RatingServiceIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private KafkaTemplate<String, RatingRequest> kafkaTemplate;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @MockBean
    private DriverClient driverClient;

    @MockBean
    private PassengerClient passengerClient;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

        registry.add("spring.kafka.producer.value-serializer", () -> JsonSerializer.class.getName());
        registry.add("spring.kafka.consumer.value-deserializer", () -> JsonDeserializer.class.getName());
        registry.add("spring.kafka.consumer.group-id", () -> "rating-group");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.properties.spring.json.trusted.packages", () -> "*");

    }


    @BeforeEach
    void setupDb() {
        ratingRepository.deleteAll();
    }

    @Test
    void createRating_ShouldReturnRatingResponse_WhenRatingCreatedSuccessfully() throws Exception {
        RatingRequest ratingRequest = new RatingRequest(1L, 2L, 5.0, "Great service");
        PassengerResponse passengerResponse = new PassengerResponse(2L, "John", "Doe", "sample@gmail.com", "123455678");
        DriverResponse driverResponse = new DriverResponse(1L, "Jane", "Doe", "Female", "12312423523", null);

        when(passengerClient.getPassengerById(2L)).thenReturn(passengerResponse);
        when(driverClient.getDriverById(1L)).thenReturn(driverResponse);

        RatingResponse createdRating = postRating(ratingRequest);

        assertThat(createdRating).isNotNull();
        assertThat(createdRating.passengerId()).isEqualTo(2L);
        assertThat(createdRating.driverId()).isEqualTo(1L);
        assertThat(createdRating.score()).isEqualTo(5);
        assertThat(createdRating.comment()).isEqualTo("Great service");
    }

    @Test
    void createRating_ShouldReturnBadRequest_WhenRatingRequestIsInvalid() throws Exception {
        RatingRequest invalidRatingRequest = new RatingRequest(null, null, null, null);

        mockMvc.perform(post("/api/v1/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRatingRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRating_ShouldReturnNotFound_WhenPassengerDoesNotExist() throws Exception {
        RatingRequest ratingRequest = new RatingRequest(1L, 999L, 5.0, "Great service");

        DriverResponse driverResponse = new DriverResponse(1L, "Jane", "Doe", "Female", "12312423523", null);
        when(driverClient.getDriverById(1L)).thenReturn(driverResponse);
        when(passengerClient.getPassengerById(999L)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(post("/api/v1/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRating_ShouldReturnNotFound_WhenDriverDoesNotExist() throws Exception {
        RatingRequest ratingRequest = new RatingRequest(999L, 2L, 5.0, "Great service");

        PassengerResponse passengerResponse = new PassengerResponse(2L, "John", "Doe", "sample@gmail.com", "123455678");
        when(passengerClient.getPassengerById(2L)).thenReturn(passengerResponse);
        when(driverClient.getDriverById(999L)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(post("/api/v1/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRatingById_ShouldReturnRatingResponse_WhenRatingExists() throws Exception {
        RatingRequest ratingRequest = new RatingRequest(1L, 2L, 5.0, "Great service");
        RatingResponse createdRating = postRating(ratingRequest);

        assert createdRating != null;
        mockMvc.perform(get("/api/v1/rating/{id}", createdRating.id())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.driverId").value(1L))
                .andExpect(jsonPath("$.passengerId").value(2L))
                .andExpect(jsonPath("$.score").value(5))
                .andExpect(jsonPath("$.comment").value("Great service"));
    }

    @Test
    void getRatingById_ShouldReturnNotFound_WhenRatingDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/ratings/{id}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateRating_ShouldReturnRatingResponse_WhenRatingUpdatedSuccessfully() throws Exception {
        RatingRequest ratingRequest = new RatingRequest(1L, 2L, 5.0, "Great service");
        RatingResponse createdRating = postRating(ratingRequest);

        RatingRequest updatedRatingRequest = new RatingRequest(1L, 2L, 4.0, "Good service");

        when(passengerClient.getPassengerById(2L)).thenReturn(new PassengerResponse(2L, "John", "Doe", "sample@gmail.com", "123455678"));
        when(driverClient.getDriverById(1L)).thenReturn(new DriverResponse(1L, "Jane", "Doe", "Female", "12312423523", null));

        mockMvc.perform(put("/api/v1/rating/{id}", createdRating.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRatingRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.score").value(4.0))
                .andExpect(jsonPath("$.comment").value("Good service"));
    }

    @Test
    void updateRating_ShouldReturnNotFound_WhenRatingDoesNotExist() throws Exception {
        RatingRequest updatedRatingRequest = new RatingRequest(1L, 2L, 4.0, "Good service");

        mockMvc.perform(put("/api/v1/ratings/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRatingRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void handleRatingEvent_ShouldSaveRating_WhenNewRatingIsProvided() throws InterruptedException {
        RatingRequest ratingRequest = new RatingRequest(1L, 2L, 5.0, "Great service");

        kafkaTemplate.send("rating-topic", ratingRequest);
        Thread.sleep(1000);
        assertThat(ratingRepository.existsByDriverIdAndPassengerId(1L, 2L)).isTrue();
    }

    @Test
    void handleRatingEvent_ShouldNotSaveDuplicateRating_WhenRatingAlreadyExists() throws InterruptedException {
        Rating existingRating = Rating.builder()
                .driverId(1L)
                .passengerId(2L)
                .score(5.0)
                .comment("Great service")
                .build();
        ratingRepository.save(existingRating);

        RatingRequest ratingRequest = new RatingRequest(1L, 2L, 5.0, "Great service");
        kafkaTemplate.send("rating-topic", ratingRequest);
        Thread.sleep(1000);

        assertThat(ratingRepository.count()).isEqualTo(1);
    }

    @Test
    void getAllRatings_ShouldReturnFilteredRatings_WhenFilterParametersAreProvided() throws Exception {
        RatingRequest ratingRequest1 = new RatingRequest(1L, 2L, 5.0, "Excellent service");
        RatingRequest ratingRequest2 = new RatingRequest(1L, 3L, 4.0, "Good service");
        RatingRequest ratingRequest3 = new RatingRequest(2L, 2L, 3.0, "Average service");

        postRating(ratingRequest1);
        postRating(ratingRequest2);
        postRating(ratingRequest3);

        mockMvc.perform(get("/api/v1/rating")
                        .param("driverId", "1")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ratings", hasSize(2)))
                .andExpect(jsonPath("$.ratings[0].score").value(5.0))
                .andExpect(jsonPath("$.ratings[1].score").value(4.0));
    }

    private RatingResponse postRating(RatingRequest ratingRequest) throws Exception {
        String json = objectMapper.writeValueAsString(ratingRequest);
        String responseString = mockMvc.perform(post("/api/v1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(responseString, RatingResponse.class);
    }
}
