package com.modsen.taxi.driversrvice.car.stepdefs;

import com.modsen.taxi.driversrvice.AccessTokenProvider;
import com.modsen.taxi.driversrvice.dto.request.CreateCarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import com.modsen.taxi.driversrvice.repository.CarRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCarSteps {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    AccessTokenProvider accessTokenProvider;

    @Autowired
    private WebTestClient client;

    private String userToken;

    @LocalServerPort
    private int port;

    private CarResponse createdCarResponse;
    private WebTestClient.ResponseSpec responseSpec;

    @Given("the car database is empty")
    public void theCarDatabaseIsEmpty() {
        carRepository.deleteAll();
    }

    @Given("the car does not already exist with license plate {string}")
    public void theCarDoesNotExist(String licensePlate) {
        carRepository.deleteByLicensePlate(licensePlate);
    }

    @When("I create a car with model {string}, color {string}, and license plate {string}")
    public void iCreateCar(String model, String color, String licensePlate) {
        CreateCarRequest carRequest = new CreateCarRequest(model, color, licensePlate);
        configureWebClient();

        responseSpec = client.post()
                .uri("/api/v1/cars")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(carRequest)
                .exchange();

        createdCarResponse = responseSpec
                .expectStatus().isCreated()
                .expectBody(CarResponse.class)
                .returnResult()
                .getResponseBody();
    }

    @Then("the car with license plate {string} should be created successfully")
    public void carShouldBeCreatedSuccessfully(String licensePlate) {
        assertThat(createdCarResponse).isNotNull();
        assertThat(createdCarResponse.licensePlate()).isEqualTo(licensePlate);
    }

    @When("I try to create a car with duplicate license plate {string}")
    public void iTryToCreateCarWithDuplicateLicensePlate(String licensePlate) {
        CreateCarRequest carRequest = new CreateCarRequest("Toyota", "Red", licensePlate);
        configureWebClient();

        responseSpec = client.post()
                .uri("/api/v1/cars")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(carRequest)
                .exchange();
    }

    @Then("an error should be returned indicating the license plate is already in use")
    public void errorShouldBeReturned() {
        responseSpec.expectStatus().is4xxClientError();
    }

    private void configureWebClient() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        userToken = accessTokenProvider.getAccessToken("user", "admin");
    }
}
