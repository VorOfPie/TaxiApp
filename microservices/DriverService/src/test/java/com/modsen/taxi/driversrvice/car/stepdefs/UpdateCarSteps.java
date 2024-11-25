package com.modsen.taxi.driversrvice.car.stepdefs;

import com.modsen.taxi.driversrvice.AccessTokenProvider;
import com.modsen.taxi.driversrvice.dto.request.CreateCarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import com.modsen.taxi.driversrvice.repository.CarRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateCarSteps {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private AccessTokenProvider accessTokenProvider;

    @Autowired
    private WebTestClient client;

    @LocalServerPort
    private int port;

    private String userToken;

    private CarResponse createdCarResponse;
    private WebTestClient.ResponseSpec responseSpec;

    @Given("the car with license plate {string} exists")
    @Transactional
    public void theCarExists(String licensePlate) {
        configureWebClient();
        CreateCarRequest carRequest = new CreateCarRequest("Toyota", "White", licensePlate);
        createdCarResponse = client.post()
                .uri("/api/v1/cars")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(carRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CarResponse.class)
                .returnResult()
                .getResponseBody();
    }

    @When("I update the car with model {string}, color {string}, and license plate {string}")
    public void iUpdateCar(String model, String color, String licensePlate) {
        CreateCarRequest updatedCarRequest = new CreateCarRequest(model, color, licensePlate);
        configureWebClient();
        responseSpec = client.put()
                .uri("/api/v1/cars/{id}", createdCarResponse.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedCarRequest)
                .exchange();

        createdCarResponse = responseSpec
                .expectStatus().isOk()
                .expectBody(CarResponse.class)
                .returnResult()
                .getResponseBody();
    }

    @Then("the car with license plate {string} should be updated successfully")
    public void carShouldBeUpdatedSuccessfully(String licensePlate) {
        assertThat(createdCarResponse).isNotNull();
        assertThat(createdCarResponse.licensePlate()).isEqualTo(licensePlate);
    }

    @Then("the updated car should have model {string}")
    public void updatedCarShouldHaveModel(String model) {
        assertThat(createdCarResponse.brand()).isEqualTo(model);
    }

    @Then("the updated car should have color {string}")
    public void updatedCarShouldHaveColor(String color) {
        assertThat(createdCarResponse.color()).isEqualTo(color);
    }

    @When("I try to update a car with id {int} and model {string}, color {string}, and license plate {string}")
    public void iTryToUpdateCarWithId(int id, String model, String color, String licensePlate) {
        CreateCarRequest updatedCarRequest = new CreateCarRequest(model, color, licensePlate);
        configureWebClient();
        responseSpec = client.put()
                .uri("/api/v1/cars/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedCarRequest)
                .exchange();
    }

    @Then("an error should be returned indicating the car does not exist for update")
    public void errorShouldBeReturnedForNonExistentCar() {
        responseSpec.expectStatus().isNotFound();
    }

    private void configureWebClient() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        userToken = accessTokenProvider.getAccessToken("user", "admin");
    }
}
