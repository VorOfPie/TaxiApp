package com.modsen.taxi.passengerservice.stepdefs;

import com.modsen.taxi.passengerservice.domain.Passenger;
import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;
import com.modsen.taxi.passengerservice.error.exception.DuplicateResourceException;
import com.modsen.taxi.passengerservice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.passengerservice.repository.PassengerRepository;
import com.modsen.taxi.passengerservice.service.PassengerService;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class PassengerStepsDefinition {

    private final PassengerService passengerService;

    private final PassengerRepository passengerRepository;

    private PassengerRequest passengerRequest;
    private String errorMessage;

    private List<PassengerResponse> passengerResponses;

    @DataTableType
    public PassengerRequest definePassengerRequestEntry(Map<String, String> entry) {
        return new PassengerRequest(
                entry.get("firstName"),
                entry.get("lastName"),
                entry.get("email"),
                entry.get("phone")
        );
    }


    @Given("the passenger does not already exist with email {string}")
    @Transactional
    public void passengerDoesNotAlreadyExist(String email) {
        passengerRepository.deletePassengerByEmail(email);
    }

    @When("I create a passenger with first name {string}, last name {string}, email {string}, phone {string}")
    public void createPassenger(String firstName, String lastName, String email, String phone) {
        passengerRequest = new PassengerRequest(firstName, lastName, email, phone);
        passengerService.createPassenger(passengerRequest).block();
    }

    @Then("the passenger with email {string} should be created successfully")
    public void passengerShouldBeCreated(String email) {
        Passenger passenger = passengerRepository.findByEmailAndIsDeletedFalse(email).orElse(null);
        Assertions.assertNotNull(passenger, "Passenger should be created");
        Assertions.assertEquals(email, passenger.getEmail(), "Emails should match");
    }

    @Then("I should retrieve the passenger with email {string}")
    public void shouldRetrievePassenger(String email) {
        Passenger passenger = passengerRepository.findByEmailAndIsDeletedFalse(email).orElse(null);
        Assertions.assertNotNull(passenger, "Passenger should be retrieved");
        Assertions.assertEquals(email, passenger.getEmail(), "Emails should match");
    }

    @Then("the passenger's first name should be {string}")
    public void verifyPassengerFirstName(String firstName) {
        Passenger passenger = passengerRepository.findByEmailAndIsDeletedFalse("sasha.kamenb@example.com").orElse(null);
        Assertions.assertNotNull(passenger, "Passenger should not be null");
        Assertions.assertEquals(firstName, passenger.getFirstName(), "First names should match");
    }

    @Then("the passenger's last name should be {string}")
    public void verifyPassengerLastName(String lastName) {
        Passenger passenger = passengerRepository.findByEmailAndIsDeletedFalse("sasha.kamenb@example.com").orElse(null);
        Assertions.assertNotNull(passenger, "Passenger should not be null");
        Assertions.assertEquals(lastName, passenger.getLastName(), "Last names should match");
    }

    @When("I delete the passenger with email {string}")
    public void deletePassenger(String email) {
        Passenger passenger = passengerRepository.findByEmailAndIsDeletedFalse(email).orElseThrow(
                () -> new RuntimeException("Passenger not found for deletion.")
        );
        passengerService.deletePassenger(passenger.getId()).block();
    }

    @Then("the passenger with email {string} should not exist")
    public void passengerShouldNotExist(String email) {
        Passenger passenger = passengerRepository.findByEmailAndIsDeletedFalse(email).orElse(null);
        Assertions.assertNull(passenger, "Passenger should be deleted and not exist in the database");
    }

    @When("I update the passenger with email {string} to first name {string}, last name {string}, phone {string}")
    public void updatePassenger(String email, String newFirstName, String newLastName, String newPhone) {
        Passenger passenger = passengerRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new RuntimeException("Passenger not found for update."));

        passengerRequest = new PassengerRequest(newFirstName, newLastName, email, newPhone);
        passengerService.updatePassenger(passenger.getId(), passengerRequest).block();
    }

    @Then("the passenger's first name should be updated to {string}")
    public void verifyUpdatedPassengerFirstName(String newFirstName) {
        Passenger passenger = passengerRepository.findByEmailAndIsDeletedFalse("sasha.kamenb@example.com").orElse(null);
        Assertions.assertNotNull(passenger, "Passenger should not be null");
        Assertions.assertEquals(newFirstName, passenger.getFirstName(), "First names should match");
    }

    @Then("the passenger's last name should be updated to {string}")
    public void verifyUpdatedPassengerLastName(String newLastName) {
        Passenger passenger = passengerRepository.findByEmailAndIsDeletedFalse("sasha.kamenb@example.com").orElse(null);
        Assertions.assertNotNull(passenger, "Passenger should not be null");
        Assertions.assertEquals(newLastName, passenger.getLastName(), "Last names should match");
    }

    @When("I try to create a passenger with duplicate email {string}")
    public void createPassengerWithDuplicateEmail(String email) {
        try {
            passengerService.createPassenger(new PassengerRequest("FirstName", "LastName", email, "1234567890")).block();
        } catch (DuplicateResourceException e) {
            errorMessage = e.getMessage();
        }
    }

    @Then("an error should be returned indicating the email is already in use")
    public void verifyDuplicateEmailError() {
        Assertions.assertNotNull(errorMessage, "Error message should not be null");
        Assertions.assertTrue(errorMessage.contains("Passenger with email"), "Error message should indicate the email is already in use");
    }

    @When("I try to retrieve a passenger with non-existent id {long}")
    public void retrievePassengerWithNonExistentId(Long nonExistentId) {
        try {
            passengerService.getPassengerById(nonExistentId).block();
        } catch (ResourceNotFoundException e) {
            errorMessage = e.getMessage();
        }
    }

    @Then("an error should be returned indicating the passenger is not found")
    public void verifyPassengerNotFoundError() {
        Assertions.assertNotNull(errorMessage, "Error message should not be null");
        Assertions.assertTrue(errorMessage.contains("Passenger with id"), "Error message should indicate the passenger is not found");
    }

    @When("I try to update a passenger with non-existent id {long} and first name {string}, last name {string}, email {string}, phone {string}")
    public void updatePassengerWithNonExistentId(Long nonExistentId, String firstName, String lastName, String email, String phone) {
        PassengerRequest passengerRequest = new PassengerRequest(firstName, lastName, email, phone);
        try {
            passengerService.updatePassenger(nonExistentId, passengerRequest).block();
        } catch (ResourceNotFoundException e) {
            errorMessage = e.getMessage();
        }
    }

    @Then("an error should be returned indicating the passenger cannot be found")
    public void verifyUpdatePassengerNotFoundError() {
        Assertions.assertNotNull(errorMessage, "Error message should not be null");
        Assertions.assertTrue(errorMessage.contains("Passenger with id"), "Error message should indicate the passenger is not found");
    }

    @Then("the response should contain {int} passengers")
    public void verifyPassengersCount(int expectedCount) {
        Assertions.assertEquals(expectedCount, passengerResponses.size(), "Passenger count should match");
    }

    @Then("the first passenger's first name should be {string}")
    public void verifyFirstPassengerFirstName(String firstName) {
        Assertions.assertEquals(firstName, passengerResponses.get(0).firstName(), "First name should match");
    }

    @Then("the first passenger's last name should be {string}")
    public void verifyFirstPassengerLastName(String lastName) {
        Assertions.assertEquals(lastName, passengerResponses.get(0).lastName(), "Last name should match");
    }


    @Given("the following passengers exist:")
    @Transactional
    public void givenTheFollowingPassengersExist(List<PassengerRequest> passengers) {
        for (PassengerRequest request : passengers) {
            if (!passengerRepository.existsByEmail(request.email())) passengerService.createPassenger(request).block();
        }
    }

    @When("I retrieve passengers with first name {string}, last name {string}, email {string}, page {int}, size {int}")
    public void retrievePassengers(String firstName, String lastName, String email, int page, int size) {
        Page<PassengerResponse> responsePage = passengerService.getAllPassengers(PageRequest.of(page, size), firstName, lastName, email, true).block();
        passengerResponses = responsePage.getContent();
    }
}
