package com.modsen.taxi.driversrvice.driver.stepdefs;

import com.modsen.taxi.driversrvice.AccessTokenProvider;
import com.modsen.taxi.driversrvice.dto.request.CarRequest;
import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
import com.modsen.taxi.driversrvice.repository.DriverRepository;
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

public class UpdateDriverSteps {

    @Autowired
    AccessTokenProvider accessTokenProvider;
    @Autowired
    private WebTestClient client;
    private String userToken;
    @LocalServerPort
    private int port;

    @Autowired
    private DriverRepository driverRepository;

    private DriverResponse createdDriverResponse;
    private WebTestClient.ResponseSpec responseSpec;
    private DriverResponse updatedDriverResponse;

    @Given("the driver with phone number {string} exists")
    public void theDriverExists(String phoneNumber) {
        List<CarRequest> cars = List.of(new CarRequest(null, "Toyota", "Camry", "ABC123"));
        DriverRequest driverRequest = new DriverRequest("John", "Doe", phoneNumber, "john.doe@example.com","Male", cars);

        createdDriverResponse = postDriver(driverRequest);
        assertThat(createdDriverResponse).isNotNull();
    }

    @When("I update the driver with first name {string}, last name {string}, phone number {string}, email {string}, gender {string}, and car license plate {string}")
    public void iUpdateDriver(String firstName, String lastName, String phoneNumber,String email, String gender, String licensePlate) {
        List<CarRequest> updatedCars = List.of(new CarRequest(null, "Honda", "Accord", licensePlate));
        DriverRequest updatedDriverRequest = new DriverRequest(firstName, lastName, phoneNumber,email, gender, updatedCars);
        configureWebClient();
        responseSpec = client.put()
                .uri("/api/v1/drivers/{id}", createdDriverResponse.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedDriverRequest)
                .exchange();

        updatedDriverResponse = responseSpec
                .expectStatus().isOk()
                .expectBody(DriverResponse.class)
                .returnResult()
                .getResponseBody();
    }

    @Then("the driver with phone number {string} should be updated successfully")
    public void driverShouldBeUpdatedSuccessfully(String expectedPhoneNumber) {
        assertThat(updatedDriverResponse).isNotNull();
        assertThat(updatedDriverResponse.phone()).isEqualTo(expectedPhoneNumber);
    }

    @Then("the updated driver should have first name {string}")
    public void updatedDriverShouldHaveFirstName(String expectedFirstName) {
        assertThat(updatedDriverResponse.firstName()).isEqualTo(expectedFirstName);
    }

    @Then("the updated driver should have last name {string}")
    public void updatedDriverShouldHaveLastName(String expectedLastName) {
        assertThat(updatedDriverResponse.lastName()).isEqualTo(expectedLastName);
    }

    @Then("the updated driver should have gender {string}")
    public void updatedDriverShouldHaveGender(String expectedGender) {
        assertThat(updatedDriverResponse.gender()).isEqualTo(expectedGender);
    }

    @Then("the updated driver should have car license plate {string}")
    public void updatedDriverShouldHaveCarLicensePlate(String expectedLicensePlate) {
        assertThat(updatedDriverResponse.cars()).hasSize(1);
        assertThat(updatedDriverResponse.cars().getFirst().licensePlate()).isEqualTo(expectedLicensePlate);
    }

    @When("I try to update a driver with id {int}, first name {string}, last name {string}, phone number {string}, email {string}, gender {string}, and car license plate {string}")
    public void iTryToUpdateADriverWithId(int id, String firstName, String lastName, String phoneNumber,String email, String gender, String licensePlate) {
        List<CarRequest> updatedCars = List.of(new CarRequest(null, "Honda", "Accord", licensePlate));
        DriverRequest updatedDriverRequest = new DriverRequest(firstName, lastName, phoneNumber,email, gender, updatedCars);
        configureWebClient();
        responseSpec = client.put()
                .uri("/api/v1/drivers/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedDriverRequest)
                .exchange();
    }

    @Then("an error should be returned indicating the driver does not exist for update")
    public void errorShouldBeReturnedForNonExistentDriver() {
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
