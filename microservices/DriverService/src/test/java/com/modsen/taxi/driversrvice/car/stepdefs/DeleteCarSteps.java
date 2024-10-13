package com.modsen.taxi.driversrvice.car.stepdefs;

import com.modsen.taxi.driversrvice.dto.request.CreateCarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteCarSteps {

    @Autowired
    private WebTestClient client;

    private CarResponse createdCarResponse;
    private WebTestClient.ResponseSpec responseSpec;

    @Given("the car with license plate {string} exists to deletion")
    @Transactional
    public void theCarExists(String licensePlate) {
        CreateCarRequest carRequest = new CreateCarRequest("Toyota", "White", licensePlate);
        createdCarResponse = postCar(carRequest);
        assertThat(createdCarResponse).isNotNull();
    }

    @When("I delete the car with license plate {string}")
    public void iDeleteTheCar(String licensePlate) {
        responseSpec = client.delete()
                .uri("/api/v1/cars/{id}", createdCarResponse.id())
                .exchange();
    }

    @Then("the car with license plate {string} should be deleted successfully")
    public void carShouldBeDeletedSuccessfully(String licensePlate) {
        responseSpec.expectStatus().isNoContent();

        client.get()
                .uri("/api/v1/cars/{id}", createdCarResponse.id())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @When("I try to delete a car with id {int}")
    public void iTryToDeleteACarWithId(int id) {
        responseSpec = client.delete()
                .uri("/api/v1/cars/{id}", id)
                .exchange();
    }

    @Then("an error should be returned indicating the car does not exist for delete")
    public void anErrorShouldBeReturnedIndicatingTheCarDoesNotExist() {
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
