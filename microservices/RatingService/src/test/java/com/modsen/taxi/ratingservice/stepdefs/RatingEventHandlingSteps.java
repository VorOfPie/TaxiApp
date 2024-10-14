package com.modsen.taxi.ratingservice.stepdefs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.taxi.ratingservice.domain.Rating;
import com.modsen.taxi.ratingservice.dto.RatingRequest;
import com.modsen.taxi.ratingservice.repository.RatingRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class RatingEventHandlingSteps {

    @Autowired
    private KafkaTemplate<String, RatingRequest> kafkaTemplate;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @When("a new rating event is received with driver ID {long}, passenger ID {long}, score {double}, and comment {string}")
    public void aNewRatingEventIsReceived(Long driverId, Long passengerId, Double score, String comment) {
        RatingRequest ratingRequest = new RatingRequest(driverId, passengerId, score, comment);
        kafkaTemplate.send("rating-topic", ratingRequest);
    }

    @Then("the rating should be saved in the database for driver ID {long} and passenger ID {long}")
    public void theRatingShouldBeSavedInDatabase(Long driverId, Long passengerId) {
        await().untilAsserted(() -> {
            assertThat(ratingRepository.existsByDriverIdAndPassengerId(driverId, passengerId)).isTrue();
        });
    }

    @Given("a rating already exists for driver ID {long} and passenger ID {long}")
    public void aRatingAlreadyExists(Long driverId, Long passengerId) {
        Rating existingRating = Rating.builder()
                .driverId(driverId)
                .passengerId(passengerId)
                .score(5.0)
                .comment("Great service")
                .build();
        ratingRepository.save(existingRating);
    }

    @Then("the rating should not be duplicated in the database")
    public void theRatingShouldNotBeDuplicatedInTheDatabase() {
        await().untilAsserted(() -> {
            assertThat(ratingRepository.count()).isEqualTo(1);
        });
    }

    @Then("the total number of ratings should be {int}")
    public void theTotalNumberOfRatingsShouldBe(int count) {
        assertThat(ratingRepository.count()).isEqualTo(count);
    }
}
