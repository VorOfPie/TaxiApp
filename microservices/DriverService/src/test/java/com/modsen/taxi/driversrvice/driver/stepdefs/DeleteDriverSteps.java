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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteDriverSteps {

    @Autowired
    private WebTestClient client;

    private DriverResponse createdDriverResponse;
    private WebTestClient.ResponseSpec responseSpec;

    @Given("the driver with phone number {string} exists for deletion")
    public void theDriverExistsForDeletion(String phoneNumber) {
        List<CarRequest> cars = List.of(new CarRequest(null, "Toyota", "Camry", "ABC123"));
        DriverRequest driverRequest = new DriverRequest("John", "Doe", phoneNumber, "Male", cars);

        createdDriverResponse = postDriver(driverRequest);
        assertThat(createdDriverResponse).isNotNull();
    }

    @When("I delete the driver with phone number {string}")
    public void iDeleteTheDriver(String phoneNumber) {
        responseSpec = client.delete()
                .uri("/api/v1/drivers/{id}", createdDriverResponse.id())
                .exchange();
    }

    @Then("the driver with phone number {string} should be deleted successfully")
    public void driverShouldBeDeletedSuccessfully(String phoneNumber) {
        responseSpec.expectStatus().isNoContent();

        client.get()
                .uri("/api/v1/drivers/{id}", createdDriverResponse.id())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @When("I try to delete a driver with id {int}")
    public void iTryToDeleteADriverWithId(int id) {
        responseSpec = client.delete()
                .uri("/api/v1/drivers/{id}", id)
                .exchange();
    }

    @Then("an error should be returned indicating the driver does not exist for delete")
    public void anErrorShouldBeReturnedIndicatingTheDriverDoesNotExist() {
        responseSpec.expectStatus().isNotFound();
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
