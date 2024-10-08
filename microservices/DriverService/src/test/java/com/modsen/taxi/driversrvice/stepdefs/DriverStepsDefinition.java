package com.modsen.taxi.driversrvice.stepdefs;

import com.modsen.taxi.driversrvice.domain.Driver;
import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
import com.modsen.taxi.driversrvice.error.exception.DuplicateResourceException;
import com.modsen.taxi.driversrvice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.driversrvice.repository.DriverRepository;
import com.modsen.taxi.driversrvice.service.DriverService;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class DriverStepsDefinition {

    private final DriverService driverService;

    private final DriverRepository driverRepository;

    private DriverRequest driverRequest;
    private String errorMessage;
    private List<DriverResponse> driverResponses;
    private String nonExistentDriverIdErrorMessage;


    @DataTableType
    public DriverRequest defineDriverRequestEntry(Map<String, String> entry) {
        return new DriverRequest(
                entry.get("firstName"),
                entry.get("lastName"),
                entry.get("phone"),
                entry.get("gender"),
                List.of()
        );
    }

    @Given("the driver does not already exist with phone {string}")
    @Transactional
    public void driverDoesNotAlreadyExist(String phone) {
        if (driverRepository.existsByPhone(phone)) {
            driverRepository.deleteDriverByPhone(phone);
        }
    }

    @When("I create a driver with first name {string}, last name {string}, phone {string}, gender {string}")
    public void createDriver(String firstName, String lastName, String phone, String gender) {
        driverRequest = new DriverRequest(firstName, lastName, phone, gender, List.of());
        try {
            driverService.createDriver(driverRequest).block();
        } catch (DuplicateResourceException e) {
            System.out.println("Driver with phone number " + phone + " already exists.");
        }
    }

    @Then("the driver with phone {string} should be created successfully")
    public void driverShouldBeCreated(String phone) {
        Driver driver = driverRepository.findDriverByPhoneAndIsDeletedFalse(phone).orElse(null);
        Assertions.assertNotNull(driver, "Driver should be created");
        Assertions.assertEquals(phone, driver.getPhone(), "Phones should match");
    }

    @Then("I should retrieve the driver with phone {string}")
    public void shouldRetrieveDriver(String phone) {
        Driver driver = driverRepository.findDriverByPhoneAndIsDeletedFalse(phone).orElse(null);
        Assertions.assertNotNull(driver, "Driver should be retrieved");
        Assertions.assertEquals(phone, driver.getPhone(), "Phones should match");
    }

    @Then("the driver's first name should be {string}")
    public void verifyDriverFirstName(String firstName) {
        Driver driver = driverRepository.findDriverByPhoneAndIsDeletedFalse("123456789").orElse(null);
        Assertions.assertNotNull(driver, "Driver should not be null");
        Assertions.assertEquals(firstName, driver.getFirstName(), "First names should match");
    }

    @Then("the driver's last name should be {string}")
    public void verifyDriverLastName(String lastName) {
        Driver driver = driverRepository.findDriverByPhoneAndIsDeletedFalse("123456789").orElse(null);
        Assertions.assertNotNull(driver, "Driver should not be null");
        Assertions.assertEquals(lastName, driver.getLastName(), "Last names should match");
    }

    @When("I delete the driver with phone {string}")
    public void deleteDriver(String phone) {
        Driver driver = driverRepository.findDriverByPhoneAndIsDeletedFalse(phone).orElseThrow(
                () -> new RuntimeException("Driver not found for deletion.")
        );
        driverService.deleteDriver(driver.getId()).block();
    }

    @Then("the driver with phone {string} should not exist")
    public void driverShouldNotExist(String phone) {
        Driver driver = driverRepository.findDriverByPhoneAndIsDeletedFalse(phone).orElse(null);
        Assertions.assertNull(driver, "Driver should be deleted and not exist in the database");
    }

    @When("I update the driver with phone {string} to first name {string}, last name {string}")
    public void updateDriverWithPhone(String phone, String newFirstName, String newLastName) {
        Driver driver = driverRepository.findDriverByPhoneAndIsDeletedFalse(phone)
                .orElseThrow(() -> new RuntimeException("Driver not found for update."));

        driverRequest = new DriverRequest(newFirstName, newLastName, driver.getPhone(), driver.getGender(), List.of());
        driverService.updateDriver(driver.getId(), driverRequest).block();
    }

    @Then("the driver's first name should be updated to {string}")
    public void verifyUpdatedDriverFirstName(String newFirstName) {
        Driver driver = driverRepository.findDriverByPhoneAndIsDeletedFalse("123456789").orElse(null);
        Assertions.assertNotNull(driver, "Driver should not be null");
        Assertions.assertEquals(newFirstName, driver.getFirstName(), "First names should match");
    }

    @Then("the driver's last name should be updated to {string}")
    public void verifyUpdatedDriverLastName(String newLastName) {
        Driver driver = driverRepository.findDriverByPhoneAndIsDeletedFalse("123456789").orElse(null);
        Assertions.assertNotNull(driver, "Driver should not be null");
        Assertions.assertEquals(newLastName, driver.getLastName(), "Last names should match");
    }

    @When("I try to create a driver with duplicate phone {string}")
    public void createDriverWithDuplicatePhone(String phone) {
        try {
            driverService.createDriver(new DriverRequest("FirstName", "LastName", phone, "gender", List.of())).block();
        } catch (DuplicateResourceException e) {
            errorMessage = e.getMessage();
        }
    }

    @Then("an error should be returned indicating the phone is already in use")
    public void verifyDuplicatePhoneError() {
        Assertions.assertNotNull(errorMessage, "Error message should not be null");
        Assertions.assertTrue(errorMessage.contains("Driver with phone"), "Error message should contain phone duplication info");
    }

    @When("I try to retrieve a driver with non-existent id {long}")
    public void tryToRetrieveDriverWithNonExistentId(Long id) {
        try {
            driverService.getDriverById(id).block();
        } catch (ResourceNotFoundException e) {
            nonExistentDriverIdErrorMessage = e.getMessage(); // Capture the error message
        }
    }

    @Then("an error should be returned indicating the driver is not found")
    public void verifyDriverNotFoundError() {
        Assertions.assertNotNull(nonExistentDriverIdErrorMessage, "Error message should not be null");
        Assertions.assertTrue(nonExistentDriverIdErrorMessage.contains("Driver not found"), "Error message should indicate the driver is not found");
    }


    @Then("the response should contain {int} drivers")
    public void thenTheResponseShouldContainDrivers(int expectedSize) {
        Assertions.assertEquals(expectedSize, driverResponses.size(), "Number of drivers should match expected count");
    }

    @When("I retrieve drivers with first name {string}, last name {string}, phone {string}, page {int}, size {int}")
    public void retrieveDriversWithFilters(String firstName, String lastName, String phone, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DriverResponse> driverPage = driverService.getAllDrivers(pageable, firstName, lastName, phone, true).block();
        driverResponses = driverPage != null ? driverPage.getContent() : List.of();
    }

    @Given("the following drivers exist:")
    @Transactional
    public void givenTheFollowingDriversExist(List<DriverRequest> drivers) {
        for (DriverRequest request : drivers) {
            if (!driverRepository.existsByPhone(request.phone())) {
                driverService.createDriver(request).block();
            }
        }
    }

    @Then("the first driver's first name should be {string}")
    public void theFirstDriversFirstNameShouldBe(String expectedFirstName) {
        Assertions.assertFalse(driverResponses.isEmpty(), "The list of drivers should not be empty");
        Assertions.assertEquals(expectedFirstName, driverResponses.get(0).firstName(), "First name should match");
    }

    @Then("the first driver's last name should be {string}")
    public void theFirstDriversLastNameShouldBe(String expectedLastName) {
        Assertions.assertFalse(driverResponses.isEmpty(), "The list of drivers should not be empty");
        Assertions.assertEquals(expectedLastName, driverResponses.get(0).lastName(), "Last name should match");
    }

}
