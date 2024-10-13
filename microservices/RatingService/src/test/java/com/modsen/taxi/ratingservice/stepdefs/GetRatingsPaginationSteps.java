package com.modsen.taxi.ratingservice.stepdefs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.taxi.ratingservice.dto.RatingRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetRatingsPaginationSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> responseBody;

    @Given("the following ratings exist:")
    public void theFollowingRatingsExist(List<Map<String, String>> ratings) throws Exception {
        for (Map<String, String> rating : ratings) {
            RatingRequest request = new RatingRequest(
                    Long.parseLong(rating.get("driverId")),
                    Long.parseLong(rating.get("passengerId")),
                    Double.parseDouble(rating.get("score")),
                    rating.get("comment")
            );
            postRating(request);
        }
    }

    @When("I get ratings filtered by driver ID {string} with page {int}, size {int}, and sorted by {string}")
    public void iGetRatingsFilteredByDriverIdWithPagination(String driverId, int page, int size, String sort) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/rating")
                        .param("driverId", driverId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("sort", sort)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        responseBody = objectMapper.readValue(content, Map.class);
    }

    @Then("the response should contain {int} ratings for driver ID {string}")
    public void theResponseShouldContainRatingsForDriverId(int expectedCount, String driverId) {
        List<Map<String, Object>> ratings = (List<Map<String, Object>>) responseBody.get("ratings");

        assertThat(ratings).hasSize(expectedCount);
        assertThat(ratings)
                .extracting(rating -> ((Number) rating.get("driverId")).longValue())
                .containsOnly(Long.parseLong(driverId));
    }

    @Then("the ratings should be sorted in ascending order of their IDs")
    public void theRatingsShouldBeSortedInAscendingOrder() {
        List<Map<String, Object>> ratings = (List<Map<String, Object>>) responseBody.get("ratings");

        List<Long> ids = ratings.stream()
                .map(rating -> ((Number) rating.get("id")).longValue())
                .collect(Collectors.toList());

        assertThat(ids).isSorted();
    }

    @Then("the current page should be {int}, total items {int}, total pages {int}")
    public void theCurrentPageShouldBe(int currentPage, int totalItems, int totalPages) {
        assertThat(responseBody.get("currentPage")).isEqualTo(currentPage);
        assertThat(responseBody.get("totalItems")).isEqualTo(totalItems);
        assertThat(responseBody.get("totalPages")).isEqualTo(totalPages);
    }

    private void postRating(RatingRequest ratingRequest) throws Exception {
        mockMvc.perform(post("/api/v1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingRequest)))
                .andExpect(status().isCreated());
    }
}
