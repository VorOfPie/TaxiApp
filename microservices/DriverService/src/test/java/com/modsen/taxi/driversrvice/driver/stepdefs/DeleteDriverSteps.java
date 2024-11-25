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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteDriverSteps {


    @Autowired
    AccessTokenProvider accessTokenProvider;
    @Autowired
    private WebTestClient client;
    private String userToken;
    @LocalServerPort
    private int port;

    private DriverResponse createdDriverResponse;
    private WebTestClient.ResponseSpec responseSpec;

    @Given("the driver with phone number {string} exists for deletion")
    public void theDriverExistsForDeletion(String phoneNumber) {
        List<CarRequest> cars = List.of(new CarRequest(null, "Toyota", "Camry", "ABC123"));
        DriverRequest driverRequest = new DriverRequest("John", "Doe", phoneNumber, "john.doe@example.com","Male",  cars);

        createdDriverResponse = postDriver(driverRequest);
        assertThat(createdDriverResponse).isNotNull();
    }

    @When("I delete the driver with phone number {string}")
    public void iDeleteTheDriver(String phoneNumber) {
        configureWebClient();
        responseSpec = client.delete()
                .uri("/api/v1/drivers/{id}", createdDriverResponse.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .exchange();
    }

    @Then("the driver with phone number {string} should be deleted successfully")
    public void driverShouldBeDeletedSuccessfully(String phoneNumber) {
        responseSpec.expectStatus().isNoContent();
        configureWebClient();
        client.get()
                .uri("/api/v1/drivers/{id}", createdDriverResponse.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @When("I try to delete a driver with id {int}")
    public void iTryToDeleteADriverWithId(int id) {
        configureWebClient();
        responseSpec = client.delete()
                .uri("/api/v1/drivers/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .exchange();
    }

    @Then("an error should be returned indicating the driver does not exist for delete")
    public void anErrorShouldBeReturnedIndicatingTheDriverDoesNotExist() {
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
