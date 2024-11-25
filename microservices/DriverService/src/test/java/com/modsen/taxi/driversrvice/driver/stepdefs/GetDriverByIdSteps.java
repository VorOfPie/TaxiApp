package com.modsen.taxi.driversrvice.driver.stepdefs;

import com.modsen.taxi.driversrvice.AccessTokenProvider;
import com.modsen.taxi.driversrvice.dto.request.CarRequest;
import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.http.HttpHeaders;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GetDriverByIdSteps {

    @Autowired
    AccessTokenProvider accessTokenProvider;
    @Autowired
    private WebTestClient client;
    private String userToken;
    @LocalServerPort
    private int port;
    private DriverResponse createdDriverResponse;
    private WebTestClient.ResponseSpec responseSpec;
    private DriverResponse driverResponse;

    @Given("the driver with phone number {string} exists to get")
    public void theDriverExists(String phoneNumber) {
        List<CarRequest> cars = List.of(new CarRequest(null, "Toyota", "Camry", "ABC123"));
        DriverRequest driverRequest = new DriverRequest("John", "Doe", phoneNumber,"john.doe@example.com", "Male", cars);

        createdDriverResponse = postDriver(driverRequest);
        assertThat(createdDriverResponse).isNotNull();
    }

    @When("I get the driver with ID")
    public void iGetTheDriverWithId() {
        configureWebClient();
        Long id = createdDriverResponse.id();
        responseSpec = client.get()
                .uri("/api/v1/drivers/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange();
    }

    @Then("the response status after driver creation should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        responseSpec.expectStatus().isEqualTo(expectedStatus);
        driverResponse = responseSpec.expectBody(DriverResponse.class).returnResult().getResponseBody();
    }

    @Then("the driver's first name should be {string}")
    public void theDriversFirstNameShouldBe(String expectedFirstName) {
        assertThat(driverResponse).isNotNull();
        assertThat(driverResponse.firstName()).isEqualTo(expectedFirstName);
    }

    @Then("the driver's last name should be {string}")
    public void theDriversLastNameShouldBe(String expectedLastName) {
        assertThat(driverResponse).isNotNull();
        assertThat(driverResponse.lastName()).isEqualTo(expectedLastName);
    }

    @Then("the driver's phone number should be {string}")
    public void theDriversPhoneNumberShouldBe(String expectedPhoneNumber) {
        assertThat(driverResponse).isNotNull();
        assertThat(driverResponse.phone()).isEqualTo(expectedPhoneNumber);
    }

    @Then("the driver's gender should be {string}")
    public void theDriversGenderShouldBe(String expectedGender) {
        assertThat(driverResponse).isNotNull();
        assertThat(driverResponse.gender()).isEqualTo(expectedGender);
    }

    @When("I try to get a driver with ID {int}")
    public void iTryToGetADriverWithId(int id) {
        configureWebClient();
        responseSpec = client.get()
                .uri("/api/v1/drivers/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange();
    }

    @Then("the driver with ID {int} should not be found")
    public void theDriverShouldNotBeFound(int id) {
        responseSpec.expectStatus().isNotFound();
    }

    private DriverResponse postDriver(DriverRequest driverRequest) {
        configureWebClient();
        return client.post()
                .uri("/api/v1/drivers")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(driverRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DriverResponse.class)
                .returnResult()
                .getResponseBody();
    }
    private void configureWebClient() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        userToken = accessTokenProvider.getAccessToken("user", "admin");
    }
}
