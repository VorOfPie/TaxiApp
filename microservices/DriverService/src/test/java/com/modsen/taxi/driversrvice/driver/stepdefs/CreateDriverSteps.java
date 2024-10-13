package com.modsen.taxi.driversrvice.driver.stepdefs;

import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
import com.modsen.taxi.driversrvice.repository.CarRepository;
import com.modsen.taxi.driversrvice.repository.DriverRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateDriverSteps {

    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private CarRepository carRepository;

    @Autowired
    private WebTestClient client;

    private DriverResponse createdDriverResponse;
    private WebTestClient.ResponseSpec responseSpec;

    @Given("the driver and car databases are empty")
    public void theDriverDatabaseIsEmpty() {
        carRepository.deleteAll();
        driverRepository.deleteAll();
    }

    @Given("I do not have any existing driver with phone number {string}")
    public void theDriverDoesNotExist(String phoneNumber) {
        driverRepository.deleteByPhone(phoneNumber);
    }

    @When("I create a driver with first name {string}, last name {string}, phone number {string}, gender {string}")
    public void iCreateDriver(String firstName, String lastName, String phoneNumber, String gender) {
        DriverRequest driverRequest = new DriverRequest(firstName, lastName, phoneNumber, gender, List.of());

        responseSpec = client.post()
                .uri("/api/v1/drivers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(driverRequest)
                .exchange();

        createdDriverResponse = responseSpec
                .expectStatus().isCreated()
                .expectBody(DriverResponse.class)
                .returnResult()
                .getResponseBody();
    }

    @Then("the driver with phone number {string} should be created successfully")
    public void driverShouldBeCreatedSuccessfully(String phoneNumber) {
        assertThat(createdDriverResponse).isNotNull();
        assertThat(createdDriverResponse.phone()).isEqualTo(phoneNumber);
    }

    @When("I create a driver with invalid phone and first name {string}, last name {string}, phone number {string}, gender {string}")
    public void iCreateDriverWithInvalidPhone(String firstName, String lastName, String phoneNumber, String gender) {
        DriverRequest driverRequest = new DriverRequest(firstName, lastName, phoneNumber, gender, List.of());

        responseSpec = client.post()
                .uri("/api/v1/drivers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(driverRequest)
                .exchange();
    }

    @Then("an error should be returned indicating the phone number is invalid")
    public void errorShouldBeReturned() {
        responseSpec.expectStatus().isBadRequest();
    }
}
