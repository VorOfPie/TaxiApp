package com.modsen.taxi.passengerservice.stepdefs

import com.modsen.taxi.passengerservice.AccessTokenProvider
import com.modsen.taxi.passengerservice.dto.PassengerRequest
import com.modsen.taxi.passengerservice.dto.PassengerResponse
import com.modsen.taxi.passengerservice.repository.PassengerRepository
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders

open class UpdatePassengerSteps {

    @Autowired
    private lateinit var passengerRepository: PassengerRepository

    @Autowired
    private lateinit var accessTokenProvider: AccessTokenProvider
    private lateinit var client: WebTestClient
    private lateinit var userToken: String

    @LocalServerPort
    private var port: Int = 0

    private var createdPassengerResponse: PassengerResponse? = null
    private lateinit var responseSpec: WebTestClient.ResponseSpec

    @Given("the passenger with email {string} exists")
    @Transactional
    open fun thePassengerExists(email: String) {
        val passengerRequest = PassengerRequest("Sasha", "Kamenb", email, "123456789")
        configureWebClient()
        createdPassengerResponse = client.post()
            .uri("/api/v1/passengers")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(passengerRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(PassengerResponse::class.java)
            .returnResult()
            .responseBody
    }

    @When("I update the passenger with first name {string}, last name {string}, email {string}, phone {string}")
    fun iUpdatePassenger(firstName: String, lastName: String, email: String, phone: String) {
        val updatedPassengerRequest = PassengerRequest(firstName, lastName, email, phone)
        configureWebClient()

        responseSpec = client.put()
            .uri("/api/v1/passengers/{id}", createdPassengerResponse?.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updatedPassengerRequest)
            .exchange()

        createdPassengerResponse = responseSpec
            .expectStatus().isOk
            .expectBody(PassengerResponse::class.java)
            .returnResult()
            .responseBody
    }

    @Then("the passenger with email {string} should be updated successfully")
    fun passengerShouldBeUpdatedSuccessfully(email: String) {
        assertThat(createdPassengerResponse).isNotNull
        assertThat(createdPassengerResponse?.email).isEqualTo(email)
    }

    @Then("the updated passenger should have phone number {string}")
    fun updatedPassengerShouldHavePhoneNumber(phone: String) {
        assertThat(createdPassengerResponse?.phone).isEqualTo(phone)
    }

    @When("I try to update a passenger with id {int} and first name {string}, last name {string}, email {string}, phone {string}")
    fun iTryToUpdatePassengerWithId(id: Int, firstName: String, lastName: String, email: String, phone: String) {
        val updatedPassengerRequest = PassengerRequest(firstName, lastName, email, phone)
        configureWebClient()
        responseSpec = client.put()
            .uri("/api/v1/passengers/{id}", id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updatedPassengerRequest)
            .exchange()
    }

    @Then("an error should be returned indicating the passenger does not exist for update")
    fun errorShouldBeReturnedForNonExistentPassenger() {
        responseSpec.expectStatus().isNotFound
    }

    private fun configureWebClient() {
        client = WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .build()
        userToken = accessTokenProvider.getAccessToken("user", "admin")
    }
}
