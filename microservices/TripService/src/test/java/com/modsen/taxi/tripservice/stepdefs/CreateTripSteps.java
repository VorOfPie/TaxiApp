package com.modsen.taxi.tripservice.stepdefs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.taxi.tripservice.config.DriverClient;
import com.modsen.taxi.tripservice.config.PassengerClient;
import com.modsen.taxi.tripservice.dto.request.TripRequest;
import com.modsen.taxi.tripservice.dto.response.DriverResponse;
import com.modsen.taxi.tripservice.dto.response.PassengerResponse;
import com.modsen.taxi.tripservice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.tripservice.repository.TripRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CreateTripSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PassengerClient passengerClient;

    @Autowired
    private DriverClient driverClient;

    @Autowired
    private TripRepository tripRepository;
    private TripRequest tripRequest;
    private PassengerResponse passengerResponse;
    private DriverResponse driverResponse;
    private ResultActions resultActions;

    @Given("the trip database is empty")
    public void thePassengerDatabaseIsEmpty() {
        tripRepository.deleteAll();
    }

    @Given("a valid trip request with origin {string}, destination {string}, driver ID {long}, passenger ID {long}, status {string}, and price {string}")
    public void aValidTripRequestWithOriginDestinationDriverIDPassengerIDStatusAndPrice(String origin, String destination, Long driverId, Long passengerId, String status, String price) {
        tripRequest = new TripRequest(driverId, passengerId, origin, destination, status, LocalDateTime.now(), new BigDecimal(price));

        passengerResponse = new PassengerResponse(passengerId, "John", "Doe", "sample@gmail.com", "123455678");
        driverResponse = new DriverResponse(driverId, "Jane", "Doe", "Female", "12312423523", null);

        when(passengerClient.getPassengerById(passengerId)).thenReturn(passengerResponse);
        when(driverClient.getDriverById(driverId)).thenReturn(driverResponse);
    }

    @When("I create a trip")
    public void iCreateATrip() throws Exception {
        resultActions = mockMvc.perform(post("/api/v1/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tripRequest)));
    }

    @When("I try to create a trip")
    public void iTryToCreateATrip() throws Exception {
        resultActions = mockMvc.perform(post("/api/v1/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tripRequest)));
    }

    @Then("the trip should be created successfully")
    public void theTripShouldBeCreatedSuccessfully() throws Exception {
        resultActions.andExpect(status().isCreated());
    }

    @Given("an invalid trip request with null fields")
    public void anInvalidTripRequestWithNullFields() {
        tripRequest = new TripRequest(null, null, null, null, null, null, null);
    }


    @Given("a valid trip request with driver ID {long} and non existing passenger ID {long}")
    public void aValidTripRequestWithDriverIDAndNonExistingPassengerID(Long driverId, Long passengerId) {
        tripRequest = new TripRequest(driverId, passengerId, "123 Origin St", "456 Destination Ave", "CREATED", LocalDateTime.now(), new BigDecimal("50.00"));

        passengerResponse = new PassengerResponse(passengerId, "John", "Doe", "sample@gmail.com", "123455678");

        when(passengerClient.getPassengerById(passengerId)).thenReturn(passengerResponse);
        when(driverClient.getDriverById(driverId)).thenThrow(new ResourceNotFoundException("Not found"));
    }

    @Given("a valid trip request with non existing driver ID {long} and passenger ID {long}")
    public void aValidTripRequestWithNonExistingDriverIDAndPassengerID(Long driverId, Long passengerId) {
        tripRequest = new TripRequest(driverId, passengerId, "123 Origin St", "456 Destination Ave", "CREATED", LocalDateTime.now(), new BigDecimal("50.00"));

        driverResponse = new DriverResponse(driverId, "Jane", "Doe", "Female", "12312423523", null);

        when(passengerClient.getPassengerById(passengerId)).thenThrow(new ResourceNotFoundException("Not found"));
        when(driverClient.getDriverById(driverId)).thenReturn(driverResponse);
    }

    @Then("I should receive a bad request status after invalid request")
    public void iShouldReceiveANotFoundStatus() throws Exception {
        resultActions.andExpect(status().is4xxClientError());
    }

    @Given("a valid trip request with driver ID {long} and passenger ID {long} and passenger ID {long}")
    public void aValidTripRequestWithDriverIDAndPassengerIDNotFound(Long driverId, Long passengerId) {
        tripRequest = new TripRequest(driverId, passengerId, "123 Origin St", "456 Destination Ave", "CREATED", LocalDateTime.now(), new BigDecimal("50.00"));

        when(passengerClient.getPassengerById(passengerId)).thenThrow(new ResourceNotFoundException("Not found"));
    }

    @Given("a valid trip request with driver ID {long} and passenger ID {long} and driver ID {long}")
    public void aValidTripRequestWithDriverIDAndPassengerIDNotFoundDriver(Long driverId, Long passengerId) {
        tripRequest = new TripRequest(driverId, passengerId, "123 Origin St", "456 Destination Ave", "CREATED", LocalDateTime.now(), new BigDecimal("50.00"));

        when(driverClient.getDriverById(driverId)).thenThrow(new ResourceNotFoundException("Not found"));
    }
}
