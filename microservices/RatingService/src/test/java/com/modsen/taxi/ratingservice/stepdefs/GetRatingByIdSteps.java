package com.modsen.taxi.ratingservice.stepdefs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.taxi.ratingservice.dto.RatingRequest;
import com.modsen.taxi.ratingservice.dto.response.RatingResponse;
import com.modsen.taxi.ratingservice.repository.RatingRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetRatingByIdSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RatingRepository ratingRepository;

    private RatingResponse createdRatingResponse;
    private ResultActions resultActions;

    @Given("a rating to get exists with driver ID {long}, passenger ID {long}, score {double}, and comment {string}")
    public void aRatingExistsWithDriverIdPassengerIdScoreAndComment(Long driverId, Long passengerId, Double score, String comment) throws Exception {
        RatingRequest ratingRequest = new RatingRequest(driverId, passengerId, score, comment);
        createdRatingResponse = postRating(ratingRequest);
        assertThat(createdRatingResponse).isNotNull();
    }

    @When("I get the rating with ID")
    public void iGetTheRatingWithId() throws Exception {
        Long id = createdRatingResponse.id();
        resultActions = mockMvc.perform(get("/api/v1/rating/{id}", id)
                .accept(MediaType.APPLICATION_JSON));
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) throws Exception {
        resultActions.andExpect(status().is(expectedStatus));
    }

    @Then("the rating's driver ID should be {long}")
    public void theRatingDriverIdShouldBe(Long expectedDriverId) throws Exception {
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.driverId").value(expectedDriverId));
    }

    @Then("the rating's passenger ID should be {long}")
    public void theRatingPassengerIdShouldBe(Long expectedPassengerId) throws Exception {
        resultActions.andExpect(jsonPath("$.passengerId").value(expectedPassengerId));
    }

    @Then("the rating's score should be {double}")
    public void theRatingScoreShouldBe(Double expectedScore) throws Exception {
        resultActions.andExpect(jsonPath("$.score").value(expectedScore));
    }

    @Then("the rating's comment should be {string}")
    public void theRatingCommentShouldBe(String expectedComment) throws Exception {
        resultActions.andExpect(jsonPath("$.comment").value(expectedComment));
    }

    @When("I try to get a rating with ID {int}")
    public void iTryToGetARatingWithId(int id) throws Exception {
        resultActions = mockMvc.perform(get("/api/v1/rating/{id}", id)
                .accept(MediaType.APPLICATION_JSON));
    }

    private RatingResponse postRating(RatingRequest ratingRequest) throws Exception {
        String jsonResponse = mockMvc.perform(post("/api/v1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(jsonResponse, RatingResponse.class);
    }
}
