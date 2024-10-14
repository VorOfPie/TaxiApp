package com.modsen.taxi.driversrvice.car.stepdefs;

import com.modsen.taxi.driversrvice.dto.request.CreateCarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
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

public class GetCarsPaginationSteps {

    @Autowired
    private WebTestClient client;

    private Map<String, Object> responseBody;

    @Given("the following cars exist:")
    public void theFollowingCarsExist(List<Map<String, String>> cars) {
        for (Map<String, String> car : cars) {
            CreateCarRequest request = new CreateCarRequest(
                    car.get("brand"),
                    car.get("color"),
                    car.get("licensePlate")
            );
            postCar(request);
        }
    }

    @When("I get cars filtered by brand {string} with page {int} and size {int}")
    public void iGetCarsFilteredByBrandWithPagination(String brand, int page, int size) {
        responseBody = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/cars")
                        .queryParam("brand", brand)
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

    @Then("the response should contain {int} cars with brand {string}")
    public void theResponseShouldContainCarsWithBrand(int expectedCount, String expectedBrand) {
        List<LinkedHashMap<String, Object>> carsMap =
                (List<LinkedHashMap<String, Object>>) responseBody.get("cars");

        List<CarResponse> cars = carsMap.stream()
                .map(map -> new CarResponse(
                        ((Number) map.get("id")).longValue(),
                        (String) map.get("brand"),
                        (String) map.get("color"),
                        (String) map.get("licensePlate")
                ))
                .collect(Collectors.toList());

        assertThat(cars).hasSize(expectedCount);
        assertThat(cars)
                .extracting(CarResponse::brand)
                .containsOnly(expectedBrand);
    }

    @Then("the current car page should be {int}, total items {int}, total pages {int}")
    public void theCurrentPageShouldBe(int currentPage, int totalItems, int totalPages) {
        assertThat(responseBody.get("currentPage")).isEqualTo(currentPage);
        assertThat(responseBody.get("totalItems")).isEqualTo(totalItems);
        assertThat(responseBody.get("totalPages")).isEqualTo(totalPages);
    }

    private CarResponse postCar(CreateCarRequest carRequest) {
        return client.post()
                .uri("/api/v1/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(carRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CarResponse.class)
                .returnResult()
                .getResponseBody();
    }
}
