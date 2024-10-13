package com.modsen.taxi.tripservice.stepdefs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.modsen.taxi.tripservice.dto.request.TripRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetTripsPaginationSteps {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult getResult;

    @Given("the following trips exist:")
    public void theFollowingTripsExist(List<Map<String, String>> tripsData) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        for (Map<String, String> tripData : tripsData) {
            TripRequest tripRequest = new TripRequest(
                    Long.parseLong(tripData.get("driverId")),
                    Long.parseLong(tripData.get("passengerId")),
                    tripData.get("origin"),
                    tripData.get("destination"),
                    tripData.get("status"),
                    LocalDateTime.parse(tripData.get("orderDateTime"), formatter),
                    new BigDecimal(tripData.get("price"))
            );

            mockMvc.perform(post("/api/v1/trips")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tripRequest)))
                    .andExpect(status().isCreated());
        }
    }

    @When("I get trips filtered by driverId {string} with page {int} and size {int}")
    public void iGetTripsFilteredByDriverIdWithPageAndSize(String driverId, int page, int size) throws Exception {
        getResult = mockMvc.perform(get("/api/v1/trips")
                        .param("driverId", driverId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("the response should contain {int} trips")
    public void theResponseShouldContainTrips(int expectedSize) throws Exception {
        String jsonResponse = getResult.getResponse().getContentAsString();
        int actualSize = JsonPath.read(jsonResponse, "$.trips.length()");
        assertEquals(expectedSize, actualSize, "The number of trips in the response is not as expected.");
    }

    @Then("the current page should be {int}, total items {int}, total pages {int}")
    public void theCurrentPageShouldBeTotalItemsTotalPages(int currentPage, int totalItems, int totalPages) throws Exception {
        String jsonResponse = getResult.getResponse().getContentAsString();
        int actualCurrentPage = JsonPath.read(jsonResponse, "$.currentPage");
        int actualTotalItems = JsonPath.read(jsonResponse, "$.totalItems");
        int actualTotalPages = JsonPath.read(jsonResponse, "$.totalPages");

        assertEquals(currentPage, actualCurrentPage, "The current page is not as expected.");
        assertEquals(totalItems, actualTotalItems, "The total number of items is not as expected.");
        assertEquals(totalPages, actualTotalPages, "The total number of pages is not as expected.");
    }

    @Then("the first trip price should be {double}")
    public void theFirstTripPriceShouldBe(double expectedPrice) throws Exception {
        String jsonResponse = getResult.getResponse().getContentAsString();
        double actualPrice = JsonPath.read(jsonResponse, "$.trips[0].price");
        assertEquals(expectedPrice, actualPrice, "The price of the first trip is not as expected.");
    }

    @Then("the second trip price should be {double}")
    public void theSecondTripPriceShouldBe(double expectedPrice) throws Exception {
        String jsonResponse = getResult.getResponse().getContentAsString();
        double actualPrice = JsonPath.read(jsonResponse, "$.trips[1].price");
        assertEquals(expectedPrice, actualPrice, "The price of the second trip is not as expected.");
    }
}
