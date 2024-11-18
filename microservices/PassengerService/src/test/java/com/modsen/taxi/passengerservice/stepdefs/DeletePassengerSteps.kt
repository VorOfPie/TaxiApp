package com.modsen.taxi.passengerservice.stepdefs

import com.modsen.taxi.passengerservice.dto.PassengerRequest
import com.modsen.taxi.passengerservice.dto.PassengerResponse
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

class DeletePassengerSteps {

    @Autowired
    private lateinit var client: WebTestClient

    private var createdPassengerResponse: PassengerResponse? = null
    private lateinit var responseSpec: WebTestClient.ResponseSpec

    @Given("the passenger with email {string} exists to deletion")
    @Transactional
    fun thePassengerExists(email: String) {
        val passengerRequest = PassengerRequest("John", "Doe", email, "+1234567890")
        createdPassengerResponse = postPassenger(passengerRequest)
        assertThat(createdPassengerResponse).isNotNull
    }

    @When("I delete the passenger with email {string}")
    fun iDeleteThePassenger(email: String) {
        responseSpec = client.delete()
            .uri("/api/v1/passengers/{id}", createdPassengerResponse?.id)
            .exchange()
    }

    @Then("the passenger with email {string} should be deleted successfully")
    fun passengerShouldBeDeletedSuccessfully(email: String) {
        responseSpec.expectStatus().isNoContent

        client.get()
            .uri("/api/v1/passengers/{id}", createdPassengerResponse?.id)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @When("I try to delete a passenger with id {int}")
    fun iTryToDeleteAPassengerWithId(id: Int) {
        responseSpec = client.delete()
            .uri("/api/v1/passengers/{id}", id)
            .exchange()
    }

    @Then("an error should be returned indicating the passenger does not exist for delete")
    fun anErrorShouldBeReturnedIndicatingThePassengerDoesNotExist() {
        responseSpec.expectStatus().isNotFound
    }

    private fun postPassenger(passengerRequest: PassengerRequest): PassengerResponse {
        return client.post()
            .uri("/api/v1/passengers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(passengerRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(PassengerResponse::class.java)
            .returnResult()
            .responseBody!!
    }
}
