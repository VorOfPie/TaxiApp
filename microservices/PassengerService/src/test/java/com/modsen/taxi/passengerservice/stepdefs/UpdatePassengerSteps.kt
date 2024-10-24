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

public class UpdatePassengerSteps {

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private WebTestClient client;

    private PassengerResponse createdPassengerResponse;
    private WebTestClient.ResponseSpec responseSpec;

    @Given("the passenger with email {string} exists")
    @Transactional
    public void thePassengerExists(String email) {
        PassengerRequest passengerRequest = new PassengerRequest("Sasha", "Kamenb", email, "123456789");
        createdPassengerResponse = client.post()
                .uri("/api/v1/passengers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(passengerRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PassengerResponse.class)
                .returnResult()
                .getResponseBody();
    }

    @When("I update the passenger with first name {string}, last name {string}, email {string}, phone {string}")
    public void iUpdatePassenger(String firstName, String lastName, String email, String phone) {
        PassengerRequest updatedPassengerRequest = new PassengerRequest(firstName, lastName, email, phone);

        responseSpec = client.put()
                .uri("/api/v1/passengers/{id}", createdPassengerResponse.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedPassengerRequest)
                .exchange();

        createdPassengerResponse = responseSpec
                .expectStatus().isOk()
                .expectBody(PassengerResponse.class)
                .returnResult()
                .getResponseBody();
    }

    @Then("the passenger with email {string} should be updated successfully")
    public void passengerShouldBeUpdatedSuccessfully(String email) {
        assertThat(createdPassengerResponse).isNotNull();
        assertThat(createdPassengerResponse.email()).isEqualTo(email);
    }

    @Then("the updated passenger should have phone number {string}")
    public void updatedPassengerShouldHavePhoneNumber(String phone) {
        assertThat(createdPassengerResponse.phone()).isEqualTo(phone);
    }

    @When("I try to update a passenger with id {int} and first name {string}, last name {string}, email {string}, phone {string}")
    public void iTryToUpdatePassengerWithId(int id, String firstName, String lastName, String email, String phone) {
        PassengerRequest updatedPassengerRequest = new PassengerRequest(firstName, lastName, email, phone);
        responseSpec = client.put()
                .uri("/api/v1/passengers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedPassengerRequest)
                .exchange();
    }

    @Then("an error should be returned indicating the passenger does not exist for update")
    public void errorShouldBeReturnedForNonExistentPassenger() {
        responseSpec.expectStatus().isNotFound();
    }
}
