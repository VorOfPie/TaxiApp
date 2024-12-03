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
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.http.HttpHeaders


open class DeletePassengerSteps {

    @Autowired
    private lateinit var accessTokenProvider: AccessTokenProvider
    private lateinit var client: WebTestClient
    private lateinit var userToken: String

    @LocalServerPort
    private var port: Int = 0
    private var createdPassengerResponse: PassengerResponse? = null
    private lateinit var responseSpec: WebTestClient.ResponseSpec

    @Given("the passenger with email {string} exists to deletion")
    @Transactional
    open fun thePassengerExists(email: String) {
        val passengerRequest = PassengerRequest("John", "Doe", email, "+1234567890")
        createdPassengerResponse = postPassenger(passengerRequest)
        assertThat(createdPassengerResponse).isNotNull
    }

    @When("I delete the passenger with email {string}")
    fun iDeleteThePassenger(email: String) {
        configureWebClient()
        responseSpec = client.delete()
            .uri("/api/v1/passengers/{id}", createdPassengerResponse?.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
            .exchange()
    }

    @Then("the passenger with email {string} should be deleted successfully")
    fun passengerShouldBeDeletedSuccessfully(email: String) {
        responseSpec.expectStatus().isNoContent
        configureWebClient()
        client.get()
            .uri("/api/v1/passengers/{id}", createdPassengerResponse?.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @When("I try to delete a passenger with id {int}")
    fun iTryToDeleteAPassengerWithId(id: Int) {
        configureWebClient()
        responseSpec = client.delete()
            .uri("/api/v1/passengers/{id}", id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
            .exchange()
    }

    @Then("an error should be returned indicating the passenger does not exist for delete")
    fun anErrorShouldBeReturnedIndicatingThePassengerDoesNotExist() {
        responseSpec.expectStatus().isNotFound
    }

    private fun postPassenger(passengerRequest: PassengerRequest): PassengerResponse {
        configureWebClient()
        return client.post()
            .uri("/api/v1/passengers")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
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
