package com.modsen.taxi.passengerservice.stepdefs

import com.modsen.taxi.passengerservice.AccessTokenProvider
import com.modsen.taxi.passengerservice.dto.PassengerRequest
import com.modsen.taxi.passengerservice.dto.PassengerResponse
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

open class GetPassengerByIdSteps {

    @Autowired
    private lateinit var accessTokenProvider: AccessTokenProvider
    private lateinit var client: WebTestClient
    private lateinit var userToken: String

    @LocalServerPort
    private var port: Int = 0

    private var createdPassengerResponse: PassengerResponse? = null
    private lateinit var responseSpec: WebTestClient.ResponseSpec
    private var passengerResponse: PassengerResponse? = null

    @Given("the passenger with email {string} exists to get")
    @Transactional
    open fun thePassengerExists(email: String) {
        val passengerRequest = PassengerRequest("Alice", "Smith", email, "+1234567890")
        createdPassengerResponse = postPassenger(passengerRequest)
        assertThat(createdPassengerResponse).isNotNull
    }

    @When("I get the passenger with ID")
    fun iGetThePassengerWithId() {
        val id = createdPassengerResponse?.id
        configureWebClient()
        responseSpec = client.get()
            .uri("/api/v1/passengers/{id}", id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
    }

    @Then("the response status should be {int}")
    fun theResponseStatusShouldBe(expectedStatus: Int) {
        responseSpec.expectStatus().isEqualTo(expectedStatus)
        passengerResponse = responseSpec.expectBody(PassengerResponse::class.java).returnResult().responseBody
    }

    @Then("the passenger's first name should be {string}")
    fun thePassengerFirstNameShouldBe(expectedFirstName: String) {
        assertThat(passengerResponse).isNotNull
        assertThat(passengerResponse?.firstName).isEqualTo(expectedFirstName)
    }

    @Then("the passenger's last name should be {string}")
    fun thePassengerLastNameShouldBe(expectedLastName: String) {
        assertThat(passengerResponse).isNotNull
        assertThat(passengerResponse?.lastName).isEqualTo(expectedLastName)
    }

    @Then("the passenger's email should be {string}")
    fun thePassengerEmailShouldBe(expectedEmail: String) {
        assertThat(passengerResponse).isNotNull
        assertThat(passengerResponse?.email).isEqualTo(expectedEmail)
    }

    @Then("the passenger's phone should be {string}")
    fun thePassengerPhoneShouldBe(expectedPhone: String) {
        assertThat(passengerResponse).isNotNull
        assertThat(passengerResponse?.phone).isEqualTo(expectedPhone)
    }

    @When("I try to get a passenger with ID {int}")
    fun iTryToGetAPassengerWithId(id: Int) {
        configureWebClient()
        responseSpec = client.get()
            .uri("/api/v1/passengers/{id}", id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
    }

    @Then("the passenger with ID {int} should not be found")
    fun thePassengerShouldNotBeFound(id: Int) {
        responseSpec.expectStatus().isNotFound
    }

    private fun postPassenger(passengerRequest: PassengerRequest): PassengerResponse {
        configureWebClient()
        return client.post()
            .uri("/api/v1/passengers")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(passengerRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(PassengerResponse::class.java)
            .returnResult()
            .responseBody!!
    }
    private fun configureWebClient() {
        client = WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .build()
        userToken = accessTokenProvider.getAccessToken("user", "admin")
    }

}
