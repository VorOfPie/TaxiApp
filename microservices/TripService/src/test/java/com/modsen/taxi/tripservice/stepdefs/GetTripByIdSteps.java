package com.modsen.taxi.tripservice.stepdefs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.taxi.tripservice.config.DriverClient;
import com.modsen.taxi.tripservice.config.PassengerClient;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class GetTripByIdSteps {

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
    @Given("a valid trip request with driver ID {long}, passenger ID {long}, origin {string}, destination {string}, status {string}, and price {string}")
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

    @When("I get the trip by id")
    public void iGetTheTripById() throws Exception {
        mockMvc.perform(get("/api/v1/trips/{id}", tripId)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Then("the trip details should be returned")
    public void theTripDetailsShouldBeReturned() throws Exception {
        mockMvc.perform(get("/api/v1/trips/{id}", tripId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.driverId").value(tripResponse.driverId()))
                .andExpect(jsonPath("$.passengerId").value(tripResponse.passengerId()))
                .andExpect(jsonPath("$.originAddress").value(tripResponse.originAddress()))
                .andExpect(jsonPath("$.destinationAddress").value(tripResponse.destinationAddress()))
                .andExpect(jsonPath("$.status").value(tripResponse.status()));
    }

    @Then("I should receive a not found status for trip")
    public void iShouldReceiveANotFoundStatusForTrip() throws Exception {
        mockMvc.perform(get("/api/v1/trips/{id}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
