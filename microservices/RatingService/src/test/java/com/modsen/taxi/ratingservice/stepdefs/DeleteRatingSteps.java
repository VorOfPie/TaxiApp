package com.modsen.taxi.ratingservice.stepdefs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.taxi.ratingservice.dto.RatingRequest;
import com.modsen.taxi.ratingservice.dto.response.RatingResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DeleteRatingSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long existingRatingId;
    private MvcResult deleteResult;

    @Given("the rating already exists for deletion")
    public void theRatingExistsForDeletion() throws Exception {
        RatingRequest ratingRequest = new RatingRequest(1L, 2L, 4.5, "Excellent service");

        MvcResult result = mockMvc.perform(post("/api/v1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingRequest)))
                .andReturn();

        RatingResponse createdRating = objectMapper.readValue(
                result.getResponse().getContentAsString(), RatingResponse.class);

        existingRatingId = createdRating.id();
    }

    @When("I delete the rating")
    public void iDeleteTheRating() throws Exception {
        mockMvc.perform(delete("/api/v1/rating/{id}", existingRatingId))
                .andExpect(status().isNoContent());
    }

    @Then("the rating should be deleted successfully")
    public void theRatingShouldBeDeletedSuccessfully() throws Exception {
        mockMvc.perform(get("/api/v1/rating/{id}", existingRatingId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @When("I try to delete a rating with id {long}")
    public void iTryToDeleteNonExistingRating(long ratingId) throws Exception {
        deleteResult = mockMvc.perform(delete("/api/v1/rating/{id}", ratingId))
                .andReturn();
    }

    @Then("an error should be returned indicating the rating does not exist for deletion")
    public void anErrorShouldBeReturnedForNonExistentRating() throws Exception {
        int status = deleteResult.getResponse().getStatus();
        assertThat(status).isEqualTo(404);

        String responseContent = deleteResult.getResponse().getContentAsString();
        assertThat(responseContent).contains("not found")
                .as("Expected error message indicating that the rating does not exist, but got: " + responseContent);
    }
}
