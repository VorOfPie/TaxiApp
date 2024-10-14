package com.modsen.taxi.ratingservice.stepdefs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.taxi.ratingservice.config.DriverClient;
import com.modsen.taxi.ratingservice.config.PassengerClient;
import com.modsen.taxi.ratingservice.dto.RatingRequest;
import com.modsen.taxi.ratingservice.dto.response.DriverResponse;
import com.modsen.taxi.ratingservice.dto.response.PassengerResponse;
import com.modsen.taxi.ratingservice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.ratingservice.repository.RatingRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CreateRatingSteps {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PassengerClient passengerClient;

    @Autowired
    private DriverClient driverClient;

    @Autowired
    private RatingRepository ratingRepository;

    private RatingRequest ratingRequest;
    private ResultActions resultActions;

    @Given("the rating database is empty")
    public void theRatingDatabaseIsEmpty() {
        ratingRepository.deleteAll();
    }

    @Given("a valid rating request with driver ID {long}, passenger ID {long}, score {double}, and comment {string}")
    public void aValidRatingRequest(Long driverId, Long passengerId, Double score, String comment) {
        ratingRequest = new RatingRequest(driverId, passengerId, score, comment);

        PassengerResponse passengerResponse = new PassengerResponse(passengerId, "John", "Doe", "sample@gmail.com", "123455678");
        DriverResponse driverResponse = new DriverResponse(driverId, "Jane", "Doe", "Female", "12312423523", null);

        when(passengerClient.getPassengerById(passengerId)).thenReturn(passengerResponse);
        when(driverClient.getDriverById(driverId)).thenReturn(driverResponse);
    }

    @When("I create a rating")
    public void iCreateARating() throws Exception {
        resultActions = mockMvc.perform(post("/api/v1/rating")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ratingRequest)));
    }

    @When("I try to create a rating")
    public void iTryToCreateARating() throws Exception {
        resultActions = mockMvc.perform(post("/api/v1/rating")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ratingRequest)));
    }

    @Then("the rating should be created successfully")
    public void theRatingShouldBeCreatedSuccessfully() throws Exception {
        resultActions.andExpect(status().isCreated());
    }

    @Given("an invalid rating request with null fields")
    public void anInvalidRatingRequestWithNullFields() {
        ratingRequest = new RatingRequest(null, null, null, null);
    }

    @Then("I should receive a bad request status")
    public void iShouldReceiveABadRequestStatus() throws Exception {
        resultActions.andExpect(status().isBadRequest());
    }

    @Given("a valid rating request with driver ID {long} and non existing passenger ID {long}")
    public void aValidRatingRequestWithNonExistingPassenger(Long driverId, Long passengerId) {
        ratingRequest = new RatingRequest(driverId, passengerId, 5.0, "Great service");

        DriverResponse driverResponse = new DriverResponse(driverId, "Jane", "Doe", "Female", "12312423523", null);
        when(driverClient.getDriverById(driverId)).thenReturn(driverResponse);
        when(passengerClient.getPassengerById(passengerId)).thenThrow(new ResourceNotFoundException("Not found"));
    }

    @Given("a valid rating request with non existing driver ID {long} and passenger ID {long}")
    public void aValidRatingRequestWithNonExistingDriver(Long driverId, Long passengerId) {
        ratingRequest = new RatingRequest(driverId, passengerId, 5.0, "Great service");

        PassengerResponse passengerResponse = new PassengerResponse(passengerId, "John", "Doe", "sample@gmail.com", "123455678");
        when(passengerClient.getPassengerById(passengerId)).thenReturn(passengerResponse);
        when(driverClient.getDriverById(driverId)).thenThrow(new ResourceNotFoundException("Not found"));
    }

    @Then("I should receive a not found status")
    public void iShouldReceiveANotFoundStatus() throws Exception {
        resultActions.andExpect(status().isNotFound());
    }
}
