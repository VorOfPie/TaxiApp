package com.modsen.taxi.tripservice.stepdefs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.taxi.tripservice.dto.request.RatingRequest;
import com.modsen.taxi.tripservice.dto.request.ScoreRequest;
import com.modsen.taxi.tripservice.dto.request.TripRequest;
import com.modsen.taxi.tripservice.dto.response.TripResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CloseAndRateTripSteps {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private KafkaConsumer<String, RatingRequest> kafkaConsumer;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    // Store the trip ID here
    private Long tripId;

    @Given("a trip with driverId {long}, passengerId {long}, origin {string}, and destination {string} exists")
    public void aTripWithDetailsExists(long driverId, long passengerId, String origin, String destination) throws Exception {
        TripRequest tripRequest = new TripRequest(driverId, passengerId, origin, destination, "CREATED",
                LocalDateTime.now(), new BigDecimal("50.00"));

        // Create the trip and capture the response
        MvcResult result = mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripRequest)))
                .andExpect(status().isCreated())
                .andReturn();


        tripId = objectMapper.readValue(result.getResponse().getContentAsString(), TripResponse.class).id();
    }

    @When("I close the trip and provide a score of {double} with comment {string}")
    public void iCloseTheTripAndProvideRating(double score, String comment) throws Exception {
        ScoreRequest scoreRequest = new ScoreRequest(score, comment);

        mockMvc.perform(post("/api/v1/trips/{id}/close", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scoreRequest)))
                .andExpect(status().isOk());
    }

    @Then("a rating message should be sent to Kafka with driverId {long}, passengerId {long}, score {double}, and comment {string}")
    public void aRatingMessageShouldBeSentToKafka(long driverId, long passengerId, double score, String comment) {
        ConsumerRecord<String, RatingRequest> record = KafkaTestUtils.getSingleRecord(kafkaConsumer, "rating-topic");

        assertThat(record).isNotNull();
        RatingRequest ratingRequest = record.value();
        assertThat(ratingRequest.driverId()).isEqualTo(driverId);
        assertThat(ratingRequest.passengerId()).isEqualTo(passengerId);
        assertThat(ratingRequest.score()).isEqualTo(score);
        assertThat(ratingRequest.comment()).isEqualTo(comment);
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
}
