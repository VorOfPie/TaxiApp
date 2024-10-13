package com.modsen.taxi.driversrvice.driver.stepdefs;

import com.modsen.taxi.driversrvice.dto.request.CarRequest;
import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
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

public class GetDriversPaginationSteps {

    @Autowired
    private WebTestClient client;

    private Map<String, Object> responseBody;

    @Given("the following drivers exist:")
    public void theFollowingDriversExist(List<Map<String, String>> drivers) {
        for (Map<String, String> driver : drivers) {
            List<CarRequest> cars = List.of(new CarRequest(
                    null,
                    driver.get("cars").split(" ")[0], // brand
                    driver.get("cars").split(" ")[1], // model
                    driver.get("cars").split(" ")[2]  // licensePlate
            ));
            DriverRequest request = new DriverRequest(
                    driver.get("firstName"),
                    driver.get("lastName"),
                    driver.get("phone"),
                    driver.get("gender"),
                    cars
            );
            postDriver(request);
        }
    }

    @When("I get drivers filtered by last name {string} with page {int} and size {int}")
    public void iGetDriversFilteredByLastNameWithPagination(String lastName, int page, int size) {
        responseBody = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/drivers")
                        .queryParam("lastName", lastName)
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

    @Then("the response should contain {int} drivers with last name {string}")
    public void theResponseShouldContainDriversWithLastName(int expectedCount, String expectedLastName) {
        List<LinkedHashMap<String, Object>> driversMap =
                (List<LinkedHashMap<String, Object>>) responseBody.get("drivers");

        List<DriverResponse> drivers = driversMap.stream()
                .map(map -> new DriverResponse(
                        ((Number) map.get("id")).longValue(),
                        (String) map.get("firstName"),
                        (String) map.get("lastName"),
                        (String) map.get("phone"),
                        (String) map.get("gender"),
                        (List) map.get("cars")
                ))
                .collect(Collectors.toList());

        assertThat(drivers).hasSize(expectedCount);
        assertThat(drivers)
                .extracting(DriverResponse::lastName)
                .containsOnly(expectedLastName);
    }

    @Then("the current driver page should be {int}, total items {int}, total pages {int}")
    public void theCurrentPageShouldBe(int currentPage, int totalItems, int totalPages) {
        assertThat(responseBody.get("currentPage")).isEqualTo(currentPage);
        assertThat(responseBody.get("totalItems")).isEqualTo(totalItems);
        assertThat(responseBody.get("totalPages")).isEqualTo(totalPages);
    }

    private DriverResponse postDriver(DriverRequest driverRequest) {
        return client.post()
                .uri("/api/v1/drivers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(driverRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DriverResponse.class)
                .returnResult()
                .getResponseBody();
    }
}
