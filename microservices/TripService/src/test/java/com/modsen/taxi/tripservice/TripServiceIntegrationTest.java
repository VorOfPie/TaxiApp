package com.modsen.taxi.tripservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.taxi.tripservice.config.DriverClient;
import com.modsen.taxi.tripservice.config.PassengerClient;
import com.modsen.taxi.tripservice.domain.TripStatus;
import com.modsen.taxi.tripservice.dto.request.RatingRequest;
import com.modsen.taxi.tripservice.dto.request.ScoreRequest;
import com.modsen.taxi.tripservice.dto.request.TripRequest;
import com.modsen.taxi.tripservice.dto.response.DriverResponse;
import com.modsen.taxi.tripservice.dto.response.PassengerResponse;
import com.modsen.taxi.tripservice.dto.response.TripResponse;
import com.modsen.taxi.tripservice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.tripservice.repository.TripRepository;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
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
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@EmbeddedKafka(partitions = 1, topics = {"rating-topic"}, brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092"})
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TripServiceIntegrationTest {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    private KafkaConsumer<String, RatingRequest> kafkaConsumer;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @MockBean
    private DriverClient driverClient;
    @MockBean
    private PassengerClient passengerClient;

    @Autowired
    private TripRepository tripRepository;
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
    }

    @PostConstruct
    public void setup() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "false", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, RatingRequest.class.getName());

        kafkaConsumer = new KafkaConsumer<>(consumerProps);
        kafkaConsumer.subscribe(Collections.singletonList("rating-topic"));
    }

    @BeforeEach
    void setupDb() {
        tripRepository.deleteAll();
    }


    @Test
    void createTrip_ShouldReturnTripResponse_WhenTripCreatedSuccessfully() throws Exception {
        TripRequest tripRequest = new TripRequest(
                1L, 2L, "123 Origin St", "456 Destination Ave", "CREATED",
                LocalDateTime.now(), new BigDecimal("50.00"));
        PassengerResponse passengerResponse = new PassengerResponse(2L, "John", "Doe", "sample@gmail.com", "123455678");
        DriverResponse driverResponse = new DriverResponse(1L, "Jane", "Doe", "Female", "12312423523", null);

        when(passengerClient.getPassengerById(2L)).thenReturn(passengerResponse);
        when(driverClient.getDriverById(1L)).thenReturn(driverResponse);
        TripResponse createdTrip = postTrip(tripRequest);

        assertThat(createdTrip).isNotNull();
        assertThat(createdTrip.driverId()).isEqualTo(1L);
        assertThat(createdTrip.passengerId()).isEqualTo(2L);
        assertThat(createdTrip.originAddress()).isEqualTo("123 Origin St");
        assertThat(createdTrip.destinationAddress()).isEqualTo("456 Destination Ave");
        assertThat(createdTrip.status()).isEqualTo("CREATED");
    }

    @Test
    void createTrip_ShouldReturnBadRequest_WhenTripRequestIsInvalid() throws Exception {
        TripRequest invalidTripRequest = new TripRequest(null, null, null, null, null, null, null);

        mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTripRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTrip_ShouldReturnNotFound_WhenPassengerDoesNotExist() throws Exception {
        TripRequest tripRequest = new TripRequest(
                1L, 999L, "123 Origin St", "456 Destination Ave", "CREATED",
                LocalDateTime.now(), new BigDecimal("50.00"));

        DriverResponse driverResponse = new DriverResponse(1L, "Jane", "Doe", "Female", "12312423523", null);
        when(driverClient.getDriverById(1L)).thenReturn(driverResponse);
        when(passengerClient.getPassengerById(999L)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTrip_ShouldReturnNotFound_WhenDriverDoesNotExist() throws Exception {
        TripRequest tripRequest = new TripRequest(
                999L, 2L, "123 Origin St", "456 Destination Ave", "CREATED",
                LocalDateTime.now(), new BigDecimal("50.00"));

        PassengerResponse passengerResponse = new PassengerResponse(2L, "John", "Doe", "sample@gmail.com", "123455678");
        when(passengerClient.getPassengerById(2L)).thenReturn(passengerResponse);
        when(driverClient.getDriverById(999L)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }


    @Test
    void getTripById_ShouldReturnTripResponse_WhenTripExists() throws Exception {
        TripRequest tripRequest = new TripRequest(
                1L, 2L, "123 Origin St", "456 Destination Ave", "CREATED",
                LocalDateTime.now(), new BigDecimal("50.00"));
        TripResponse createdTrip = postTrip(tripRequest);

        assert createdTrip != null;
        mockMvc.perform(get("/api/v1/trips/{id}", createdTrip.id())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.driverId").value(1L))
                .andExpect(jsonPath("$.passengerId").value(2L))
                .andExpect(jsonPath("$.originAddress").value("123 Origin St"))
                .andExpect(jsonPath("$.destinationAddress").value("456 Destination Ave"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getTripById_ShouldReturnNotFound_WhenTripDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/trips/{id}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTrip_ShouldReturnUpdatedTripResponse_WhenTripExists() throws Exception {
        TripRequest tripRequest = new TripRequest(
                1L, 2L, "123 Origin St", "456 Destination Ave", "CREATED",
                LocalDateTime.now(), new BigDecimal("50.00"));
        PassengerResponse passengerResponse = new PassengerResponse(2L, "John", "Doe", "sample@gmail.com", "123455678");
        DriverResponse driverResponse = new DriverResponse(1L, "Jane", "Doe", "Female", "12312423523", null);

        when(passengerClient.getPassengerById(2L)).thenReturn(passengerResponse);
        when(driverClient.getDriverById(1L)).thenReturn(driverResponse);
        TripResponse createdTrip = postTrip(tripRequest);

        TripRequest updatedTripRequest = new TripRequest(
                1L, 2L, "123 Origin St", "789 Updated Ave", TripStatus.CREATED.name(),
                LocalDateTime.now(), new BigDecimal("75.0"));

        mockMvc.perform(put("/api/v1/trips/{id}", createdTrip.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTripRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destinationAddress").value("789 Updated Ave"))
                .andExpect(jsonPath("$.status").value(TripStatus.CREATED.name()))
                .andExpect(jsonPath("$.price").value(new BigDecimal("75.0")));
    }

    @Test
    void updateTrip_ShouldReturnNotFound_WhenTripDoesNotExist() throws Exception {
        TripRequest updatedTripRequest = new TripRequest(
                1L, 2L, "123 Origin St", "789 Updated Ave", TripStatus.CREATED.name(),
                LocalDateTime.now(), new BigDecimal("75.0"));

        mockMvc.perform(put("/api/v1/trips/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTripRequest)))
                .andExpect(status().isNotFound());
    }


    @Test
    void deleteTrip_ShouldReturnNoContent_WhenTripDeletedSuccessfully() throws Exception {
        TripRequest tripRequest = new TripRequest(
                1L, 2L, "123 Origin St", "456 Destination Ave", "CREATED",
                LocalDateTime.now(), new BigDecimal("50.00"));

        TripResponse createdTrip = postTrip(tripRequest);

        mockMvc.perform(delete("/api/v1/trips/{id}", createdTrip.id()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/trips/{id}", createdTrip.id())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTrip_ShouldReturnNotFound_WhenTripDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/v1/trips/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void closeAndRateTrip_ShouldSendRatingMessageToKafka_WhenTripIsClosed() throws Exception {
        TripRequest tripRequest = new TripRequest(1L, 2L, "123 Origin St", "456 Destination Ave", "CREATED",
                LocalDateTime.now(), new BigDecimal("50.00"));
        TripResponse createdTrip = postTrip(tripRequest);

        ScoreRequest scoreRequest = new ScoreRequest(5.0, "Great trip!");

        mockMvc.perform(post("/api/v1/trips/{id}/close", createdTrip.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scoreRequest)))
                .andExpect(status().isOk());

        ConsumerRecord<String, RatingRequest> record = KafkaTestUtils.getSingleRecord(kafkaConsumer, "rating-topic");
        assertThat(record).isNotNull();

        RatingRequest ratingRequest = record.value();
        assertThat(ratingRequest.driverId()).isEqualTo(createdTrip.driverId());
        assertThat(ratingRequest.passengerId()).isEqualTo(createdTrip.passengerId());
        assertThat(ratingRequest.score()).isEqualTo(scoreRequest.score());
        assertThat(ratingRequest.comment()).isEqualTo(scoreRequest.comment());
    }

    @Test
    void getAllTrips_ShouldReturnFilteredTrips_WhenFilterParametersAreProvided() throws Exception {
        TripRequest tripRequest1 = new TripRequest(
                1L,
                1L,
                "Address 1",
                "Destination 1",
                "CREATED",
                LocalDateTime.now().minusDays(1),
                BigDecimal.valueOf(5.0)
        );

        TripRequest tripRequest2 = new TripRequest(
                1L,
                2L,
                "Address 2",
                "Destination 2",
                "CREATED",
                LocalDateTime.now().minusDays(1),
                BigDecimal.valueOf(10.0)
        );

        TripRequest tripRequest3 = new TripRequest(
                2L,
                1L,
                "Address 3",
                "Destination 3",
                "CREATED",
                LocalDateTime.now().minusDays(1),
                BigDecimal.valueOf(0.01)
        );

        postTrip(tripRequest1);
        postTrip(tripRequest2);
        postTrip(tripRequest3);

        mockMvc.perform(get("/api/v1/trips")
                        .param("driverId", "1")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.trips", hasSize(2)))
                .andExpect(jsonPath("$.trips[0].price").value(5.0))
                .andExpect(jsonPath("$.trips[1].price").value(10.0));

    }



    private TripResponse postTrip(TripRequest tripRequest) throws Exception {
        String response = mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, TripResponse.class);
    }
}
