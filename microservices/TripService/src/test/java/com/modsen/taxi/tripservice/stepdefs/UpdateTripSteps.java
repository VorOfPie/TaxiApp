package com.modsen.taxi.tripservice.stepdefs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.taxi.tripservice.config.DriverClient;
import com.modsen.taxi.tripservice.config.PassengerClient;
import com.modsen.taxi.tripservice.domain.TripStatus;
import com.modsen.taxi.tripservice.dto.request.TripRequest;
import com.modsen.taxi.tripservice.dto.response.DriverResponse;
import com.modsen.taxi.tripservice.dto.response.PassengerResponse;
import com.modsen.taxi.tripservice.dto.response.TripResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UpdateTripSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PassengerClient passengerClient;

    @Autowired
    private DriverClient driverClient;

    private TripRequest tripRequest;
    private TripResponse tripResponse;
    private Long tripId;

    @Given("a valid trip request for update with driver ID {long}, passenger ID {long}, origin {string}, destination {string}, status {string}, and price {string}")
    public void aValidTripRequest(Long driverId, Long passengerId, String origin, String destination, String status, String price) throws Exception {
        tripRequest = new TripRequest(driverId, passengerId, origin, destination, status, LocalDateTime.now(), new BigDecimal(price));

        when(passengerClient.getPassengerById(passengerId)).thenReturn(new PassengerResponse(passengerId, "John", "Doe", "sample@gmail.com", "123455678"));
        when(driverClient.getDriverById(driverId)).thenReturn(new DriverResponse(driverId, "Jane", "Doe", "Female", "12312423523", null));

        String jsonResponse = mockMvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        tripResponse = objectMapper.readValue(jsonResponse, TripResponse.class);
        tripId = tripResponse.id();
    }

    @When("I update the trip with new destination {string} and new price {string}")
    public void iUpdateTheTripWithNewDestinationAndPrice(String newDestination, String newPrice) throws Exception {
        TripRequest updatedTripRequest = new TripRequest(
                tripResponse.driverId(),
                tripResponse.passengerId(),
                tripResponse.originAddress(),
                newDestination,
                tripResponse.status(),
                LocalDateTime.now(),
                new BigDecimal(newPrice));

        mockMvc.perform(put("/api/v1/trips/{id}", tripId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTripRequest)))
                .andReturn();
    }

    @Then("the trip should be updated successfully with destination {string}, status {string}, and price {string}")
    public void theTripShouldBeUpdatedSuccessfully(String expectedDestination, String expectedStatus, String expectedPrice) throws Exception {
        mockMvc.perform(get("/api/v1/trips/{id}", tripId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destinationAddress").value(expectedDestination))
                .andExpect(jsonPath("$.status").value(expectedStatus))
                .andExpect(jsonPath("$.price").value(new BigDecimal(expectedPrice)));
    }

    @When("I try to update a trip with id {long} and new destination {string}, new price {string}")
    public void tryToUpdateTrip(long tripId, String newDestination, String newPrice) throws Exception {
        tripRequest = new TripRequest(
                1L,
                2L,
                "123 Origin St",
                newDestination,
                TripStatus.CREATED.name(),
                LocalDateTime.now(),
                new BigDecimal(newPrice)
        );

        mockMvc.perform(put("/api/v1/trips/{id}", tripId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tripRequest)));
    }

    @Then("an error should be returned indicating the trip does not exist for update")
    public void verifyTripNotFoundError() throws Exception {
        mockMvc.perform(put("/api/v1/trips/{id}", 9999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripRequest)))
                .andExpect(status().isNotFound());
    }

}
