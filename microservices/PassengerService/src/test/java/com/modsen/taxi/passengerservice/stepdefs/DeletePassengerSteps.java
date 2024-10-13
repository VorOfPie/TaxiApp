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

public class DeletePassengerSteps {

    @Autowired
    private WebTestClient client;

    private PassengerResponse createdPassengerResponse;
    private WebTestClient.ResponseSpec responseSpec;

    @Given("the passenger with email {string} exists to deletion")
    @Transactional
    public void thePassengerExists(String email) {
        PassengerRequest passengerRequest = new PassengerRequest("John", "Doe", email, "+1234567890");
        createdPassengerResponse = postPassenger(passengerRequest);
        assertThat(createdPassengerResponse).isNotNull();
    }

    @When("I delete the passenger with email {string}")
    public void iDeleteThePassenger(String email) {
        responseSpec = client.delete()
                .uri("/api/v1/passengers/{id}", createdPassengerResponse.id())
                .exchange();
    }

    @Then("the passenger with email {string} should be deleted successfully")
    public void passengerShouldBeDeletedSuccessfully(String email) {
        responseSpec.expectStatus().isNoContent();

        client.get()
                .uri("/api/v1/passengers/{id}", createdPassengerResponse.id())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @When("I try to delete a passenger with id {int}")
    public void iTryToDeleteAPassengerWithId(int id) {
        responseSpec = client.delete()
                .uri("/api/v1/passengers/{id}", id)
                .exchange();
    }

    @Then("an error should be returned indicating the passenger does not exist for delete")
    public void anErrorShouldBeReturnedIndicatingThePassengerDoesNotExist() {
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
