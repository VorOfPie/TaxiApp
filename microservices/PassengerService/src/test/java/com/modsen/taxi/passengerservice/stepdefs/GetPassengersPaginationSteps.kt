package com.modsen.taxi.passengerservice.stepdefs;

import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class GetPassengersPaginationSteps {

    @Autowired
    private WebTestClient client;

    private Map<String, Object> responseBody;

    @Given("the following passengers exist:")
    public void theFollowingPassengersExist(List<Map<String, String>> passengers) {
        for (Map<String, String> passenger : passengers) {
            PassengerRequest request = new PassengerRequest(
                    passenger.get("firstName"),
                    passenger.get("lastName"),
                    passenger.get("email"),
                    passenger.get("phone")
            );
            postPassenger(request);
        }
    }

    @When("I get passengers filtered by first name {string} with page {int} and size {int}")
    public void iGetPassengersFilteredByFirstNameWithPagination(String firstName, int page, int size) {
        responseBody = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/passengers")
                        .queryParam("firstName", firstName)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
    }

    @Then("the response should contain {int} passengers with first name {string}")
    public void theResponseShouldContainPassengersWithFirstName(int expectedCount, String expectedFirstName) {
        List<LinkedHashMap<String, Object>> passengersMap =
                (List<LinkedHashMap<String, Object>>) responseBody.get("passengers");

        List<PassengerResponse> passengers = passengersMap.stream()
                .map(map -> new PassengerResponse(
                        ((Number) map.get("id")).longValue(),
                        (String) map.get("firstName"),
                        (String) map.get("lastName"),
                        (String) map.get("email"),
                        (String) map.get("phone")
                ))
                .collect(Collectors.toList());

        assertThat(passengers).hasSize(expectedCount);
        assertThat(passengers)
                .extracting(PassengerResponse::firstName)
                .containsOnly(expectedFirstName);
    }

    @Then("the current page should be {int}, total items {int}, total pages {int}")
    public void theCurrentPageShouldBe(int currentPage, int totalItems, int totalPages) {
        assertThat(responseBody.get("currentPage")).isEqualTo(currentPage);
        assertThat(responseBody.get("totalItems")).isEqualTo(totalItems);
        assertThat(responseBody.get("totalPages")).isEqualTo(totalPages);
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
