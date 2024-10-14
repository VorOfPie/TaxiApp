package com.modsen.taxi.tripservice.stepdefs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.taxi.tripservice.dto.request.TripRequest;
import com.modsen.taxi.tripservice.dto.response.TripResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DeleteTripSteps {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long existingTripId;
    private MvcResult deleteResult;


    @Given("the trip already exists for deletion")
    public void theTripWithIdExistsForDeletion() throws Exception {
        TripRequest tripRequest = new TripRequest(
                1L, 2L, "123 Origin St", "456 Destination Ave", "CREATED",
                LocalDateTime.now(), new BigDecimal("50.00"));

        MvcResult result = mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripRequest)))
                .andReturn();

        TripResponse createdTrip = objectMapper.readValue(
                result.getResponse().getContentAsString(), TripResponse.class);

        existingTripId = createdTrip.id();
    }

    @When("I delete the trip")
    public void iDeleteTheTripWithId() throws Exception {
        mockMvc.perform(delete("/api/v1/trips/{id}", existingTripId))
                .andExpect(status().isNoContent());
    }

    @Then("the trip should be deleted successfully")
    public void theTripShouldBeDeletedSuccessfully() throws Exception {
        mockMvc.perform(get("/api/v1/trips/{id}", existingTripId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @When("I try to delete a trip with id {long}")
    public void iTryToDeleteNonExistingTrip(long tripId) throws Exception {
        deleteResult = mockMvc.perform(delete("/api/v1/trips/{id}", tripId))
                .andReturn();
    }

    @Then("an error should be returned indicating the trip does not exist for deletion")
    public void anErrorShouldBeReturnedForNonExistentTrip() throws Exception {
        int status = deleteResult.getResponse().getStatus();
        assertTrue(status == 404, "Expected status 404 but got: " + status);

        String responseContent = deleteResult.getResponse().getContentAsString();
        assertTrue(responseContent.contains("not found") || responseContent.contains("does not exist"),
                "Expected error message indicating that the trip does not exist, but got: " + responseContent);
    }
}
