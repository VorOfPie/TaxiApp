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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UpdateRatingSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RatingRepository ratingRepository;

    private RatingResponse createdRatingResponse;
    private ResultActions resultActions;

    @Given("a rating to update exists with driver ID {long}, passenger ID {long}, score {double}, and comment {string}")
    public void aRatingExistsWithDriverIdPassengerIdScoreAndComment(Long driverId, Long passengerId, Double score, String comment) throws Exception {
        RatingRequest ratingRequest = new RatingRequest(driverId, passengerId, score, comment);
        createdRatingResponse = postRating(ratingRequest);
        assertThat(createdRatingResponse).isNotNull();
    }

    @When("I update the rating with score {double} and comment {string}")
    public void iUpdateRatingWithScoreAndComment(Double score, String comment) throws Exception {
        RatingRequest updatedRatingRequest = new RatingRequest(1L, 2L, score, comment);

        resultActions = mockMvc.perform(put("/api/v1/rating/{id}", createdRatingResponse.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedRatingRequest)));
    }

    @Then("the rating with ID should be updated successfully")
    public void ratingShouldBeUpdatedSuccessfully() throws Exception {
        resultActions.andExpect(status().isOk());
    }

    @Then("the updated rating should have a score of {double}")
    public void updatedRatingShouldHaveScore(Double expectedScore) throws Exception {
        resultActions.andExpect(jsonPath("$.score").value(expectedScore));
    }

    @Then("the updated rating should have a comment {string}")
    public void updatedRatingShouldHaveComment(String expectedComment) throws Exception {
        resultActions.andExpect(jsonPath("$.comment").value(expectedComment));
    }

    @When("I try to update a rating with id {int} with score {double} and comment {string}")
    public void iTryToUpdateRatingWithId(int id, Double score, String comment) throws Exception {
        RatingRequest updatedRatingRequest = new RatingRequest(1L, 2L, score, comment);

        resultActions = mockMvc.perform(put("/api/v1/rating/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedRatingRequest)));
    }

    @Then("an error should be returned indicating the rating does not exist for update")
    public void errorShouldBeReturnedForNonExistentRating() throws Exception {
        resultActions.andExpect(status().isNotFound());
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
