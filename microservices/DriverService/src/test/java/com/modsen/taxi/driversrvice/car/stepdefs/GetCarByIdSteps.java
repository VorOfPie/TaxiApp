package com.modsen.taxi.driversrvice.car.stepdefs;

import com.modsen.taxi.driversrvice.dto.request.CreateCarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GetCarByIdSteps {

    @Autowired
    private WebTestClient client;

    private CarResponse createdCarResponse;
    private WebTestClient.ResponseSpec responseSpec;
    private CarResponse carResponse;

    @Given("the car with license plate {string} exists to get")
    public void theCarExists(String licensePlate) {
        CreateCarRequest carRequest = new CreateCarRequest("Toyota", "White", licensePlate);
        createdCarResponse = postCar(carRequest);
        assertThat(createdCarResponse).isNotNull();
    }

    @When("I get the car with ID")
    public void iGetTheCarWithId() {
        Long id = createdCarResponse.id();
        responseSpec = client.get()
                .uri("/api/v1/cars/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange();
    }

    @Then("the response status after car creation should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        responseSpec.expectStatus().isEqualTo(expectedStatus);
        carResponse = responseSpec.expectBody(CarResponse.class).returnResult().getResponseBody();
    }

    @Then("the car's brand should be {string}")
    public void theCarBrandShouldBe(String expectedBrand) {
        assertThat(carResponse).isNotNull();
        assertThat(carResponse.brand()).isEqualTo(expectedBrand);
    }

    @Then("the car's color should be {string}")
    public void theCarColorShouldBe(String expectedColor) {
        assertThat(carResponse).isNotNull();
        assertThat(carResponse.color()).isEqualTo(expectedColor);
    }

    @Then("the car's license plate should be {string}")
    public void theCarLicensePlateShouldBe(String expectedLicensePlate) {
        assertThat(carResponse).isNotNull();
        assertThat(carResponse.licensePlate()).isEqualTo(expectedLicensePlate);
    }

    @When("I try to get a car with ID {int}")
    public void iTryToGetACarWithId(int id) {
        responseSpec = client.get()
                .uri("/api/v1/cars/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange();
    }

    @Then("the car with ID {int} should not be found")
    public void theCarShouldNotBeFound(int id) {
        responseSpec.expectStatus().isNotFound();
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
