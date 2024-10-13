package com.modsen.taxi.passengerservice.stepdefs;

import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;
import com.modsen.taxi.passengerservice.repository.PassengerRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

public class CreatePassengerSteps {

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private WebTestClient client;

    private PassengerResponse createdPassengerResponse;
    private WebTestClient.ResponseSpec responseSpec;

    @Given("the passenger database is empty")
    public void thePassengerDatabaseIsEmpty() {
        passengerRepository.deleteAll();
    }

    @Given("the passenger does not already exist with email {string}")
    @Transactional
    public void thePassengerDoesNotExist(String email) {
        passengerRepository.deleteByEmail(email);
    }

    @When("I create a passenger with first name {string}, last name {string}, email {string}, phone {string}")
    public void iCreatePassenger(String firstName, String lastName, String email, String phone) {
        PassengerRequest passengerRequest = new PassengerRequest(firstName, lastName, email, phone);

        responseSpec = client.post()
                .uri("/api/v1/passengers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(passengerRequest)
                .exchange();

        createdPassengerResponse = responseSpec
                .expectStatus().isCreated()
                .expectBody(PassengerResponse.class)
                .returnResult()
                .getResponseBody();
    }

    @Then("the passenger with email {string} should be created successfully")
    public void passengerShouldBeCreatedSuccessfully(String email) {
        assertThat(createdPassengerResponse).isNotNull();
        assertThat(createdPassengerResponse.email()).isEqualTo(email);
    }

    @When("I try to create a passenger with duplicate email {string}")
    public void iTryToCreatePassengerWithDuplicateEmail(String email) {
        PassengerRequest passengerRequest = new PassengerRequest("Sasha", "Kamenb", email, "1234567890");

        responseSpec = client.post()
                .uri("/api/v1/passengers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(passengerRequest)
                .exchange();
    }

    @Then("an error should be returned indicating the email is already in use")
    public void errorShouldBeReturned() {
        responseSpec.expectStatus().is4xxClientError();
    }
}
