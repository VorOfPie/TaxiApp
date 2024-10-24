package com.modsen.taxi.passengerservice.stepdefs

import com.modsen.taxi.passengerservice.dto.PassengerRequest
import com.modsen.taxi.passengerservice.dto.PassengerResponse
import com.modsen.taxi.passengerservice.repository.PassengerRepository
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

class CreatePassengerSteps {

    @Autowired
    private lateinit var passengerRepository: PassengerRepository

    @Autowired
    private lateinit var client: WebTestClient

    private var createdPassengerResponse: PassengerResponse? = null
    private lateinit var responseSpec: WebTestClient.ResponseSpec

    @Given("the passenger database is empty")
    fun thePassengerDatabaseIsEmpty() {
        passengerRepository.deleteAll()
    }

    @Given("the passenger does not already exist with email {string}")
    @Transactional
    fun thePassengerDoesNotExist(email: String) {
        passengerRepository.deleteByEmail(email)
    }

    @When("I create a passenger with first name {string}, last name {string}, email {string}, phone {string}")
    fun iCreatePassenger(firstName: String, lastName: String, email: String, phone: String) {
        val passengerRequest = PassengerRequest(firstName, lastName, email, phone)

        responseSpec = client.post()
            .uri("/api/v1/passengers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(passengerRequest)
            .exchange()

        createdPassengerResponse = responseSpec
            .expectStatus().isCreated
            .expectBody(PassengerResponse::class.java)
            .returnResult()
            .responseBody
    }

    @Then("the passenger with email {string} should be created successfully")
    fun passengerShouldBeCreatedSuccessfully(email: String) {
        assertThat(createdPassengerResponse).isNotNull
        assertThat(createdPassengerResponse?.email).isEqualTo(email)
    }

    @When("I try to create a passenger with duplicate email {string}")
    fun iTryToCreatePassengerWithDuplicateEmail(email: String) {
        val passengerRequest = PassengerRequest("Sasha", "Kamenb", email, "1234567890")

        responseSpec = client.post()
            .uri("/api/v1/passengers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(passengerRequest)
            .exchange()
    }

    @Then("an error should be returned indicating the email is already in use")
    fun errorShouldBeReturned() {
        responseSpec.expectStatus().is4xxClientError
    }
}
