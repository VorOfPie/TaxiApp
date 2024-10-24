package com.modsen.taxi.passengerservice.stepdefs;

import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

public class GetPassengerByIdSteps {

    @Autowired
    private WebTestClient client;

    private PassengerResponse createdPassengerResponse;
    private WebTestClient.ResponseSpec responseSpec;
    private PassengerResponse passengerResponse;

    @Given("the passenger with email {string} exists to get")
    @Transactional
    public void thePassengerExists(String email) {
        PassengerRequest passengerRequest = new PassengerRequest("Alice", "Smith", email, "+1234567890");
        createdPassengerResponse = postPassenger(passengerRequest);
        assertThat(createdPassengerResponse).isNotNull();
    }

    @When("I get the passenger with ID")
    public void iGetThePassengerWithId() {
        Long id = createdPassengerResponse.id();
        responseSpec = client.get()
                .uri("/api/v1/passengers/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange();
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        responseSpec.expectStatus().isEqualTo(expectedStatus);
        passengerResponse = responseSpec.expectBody(PassengerResponse.class).returnResult().getResponseBody();
    }

    @Then("the passenger's first name should be {string}")
    public void thePassengerFirstNameShouldBe(String expectedFirstName) {
        assertThat(passengerResponse).isNotNull();
        assertThat(passengerResponse.firstName()).isEqualTo(expectedFirstName);
    }

    @Then("the passenger's last name should be {string}")
    public void thePassengerLastNameShouldBe(String expectedLastName) {
        assertThat(passengerResponse).isNotNull();
        assertThat(passengerResponse.lastName()).isEqualTo(expectedLastName);
    }

    @Then("the passenger's email should be {string}")
    public void thePassengerEmailShouldBe(String expectedEmail) {
        assertThat(passengerResponse).isNotNull();
        assertThat(passengerResponse.email()).isEqualTo(expectedEmail);
    }

    @Then("the passenger's phone should be {string}")
    public void thePassengerPhoneShouldBe(String expectedPhone) {
        assertThat(passengerResponse).isNotNull();
        assertThat(passengerResponse.phone()).isEqualTo(expectedPhone);
    }

    @When("I try to get a passenger with ID {int}")
    public void iTryToGetAPassengerWithId(int id) {
        responseSpec = client.get()
                .uri("/api/v1/passengers/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange();
    }

    @Then("the passenger with ID {int} should not be found")
    public void thePassengerShouldNotBeFound(int id) {
        responseSpec.expectStatus().isNotFound();
    }

    private PassengerResponse postPassenger(PassengerRequest passengerRequest) {
        return client.post()
                .uri("/api/v1/passengers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(passengerRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PassengerResponse.class)
                .returnResult()
                .getResponseBody();
    }
}
