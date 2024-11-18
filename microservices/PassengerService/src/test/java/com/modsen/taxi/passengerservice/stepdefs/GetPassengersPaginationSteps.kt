package com.modsen.taxi.passengerservice.stepdefs

import com.modsen.taxi.passengerservice.dto.PassengerRequest
import com.modsen.taxi.passengerservice.dto.PassengerResponse
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.LinkedHashMap

class GetPassengersPaginationSteps {

    @Autowired
    private lateinit var client: WebTestClient

    private lateinit var responseBody: Map<String, Any>

    @Given("the following passengers exist:")
    fun theFollowingPassengersExist(passengers: List<Map<String, String>>) {
        for (passenger in passengers) {
            val request = PassengerRequest(
                firstName = passenger["firstName"]?:"",
                lastName = passenger["lastName"]?:"",
                email = passenger["email"]?:"",
                phone = passenger["phone"]?:""
            )
            postPassenger(request)
        }
    }

    @When("I get passengers filtered by first name {string} with page {int} and size {int}")
    fun iGetPassengersFilteredByFirstNameWithPagination(firstName: String, page: Int, size: Int) {
        responseBody = client.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/v1/passengers")
                    .queryParam("firstName", firstName)
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(Map::class.java)
            .returnResult()
            .responseBody as Map<String, Any>
    }

    @Then("the response should contain {int} passengers with first name {string}")
    fun theResponseShouldContainPassengersWithFirstName(expectedCount: Int, expectedFirstName: String) {
        val passengersMap = responseBody["passengers"] as List<LinkedHashMap<String, Any>>

        val passengers = passengersMap.map { map ->
            PassengerResponse(
                id = (map["id"] as Number).toLong(),
                firstName = map["firstName"] as String,
                lastName = map["lastName"] as String,
                email = map["email"] as String,
                phone = map["phone"] as String
            )
        }

        assertThat(passengers).hasSize(expectedCount)

        val firstNames = passengers.map { it.firstName }
        assertThat(firstNames).containsOnly(expectedFirstName)
    }


    @Then("the current page should be {int}, total items {int}, total pages {int}")
    fun theCurrentPageShouldBe(currentPage: Int, totalItems: Int, totalPages: Int) {
        assertThat(responseBody["currentPage"]).isEqualTo(currentPage)
        assertThat(responseBody["totalItems"]).isEqualTo(totalItems)
        assertThat(responseBody["totalPages"]).isEqualTo(totalPages)
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
