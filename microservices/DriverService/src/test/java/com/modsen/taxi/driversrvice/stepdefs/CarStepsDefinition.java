package com.modsen.taxi.driversrvice.stepdefs;

import com.modsen.taxi.driversrvice.domain.Car;
import com.modsen.taxi.driversrvice.dto.request.CreateCarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import com.modsen.taxi.driversrvice.error.exception.DuplicateResourceException;
import com.modsen.taxi.driversrvice.error.exception.ResourceNotFoundException;
import com.modsen.taxi.driversrvice.repository.CarRepository;
import com.modsen.taxi.driversrvice.service.CarService;
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
public class CarStepsDefinition {

    private final CarService carService;

    private final CarRepository carRepository;

    private CreateCarRequest CreateCarRequest;
    private String errorMessage;
    private List<CarResponse> carResponses;
    private String nonExistentCarIdErrorMessage;

    @DataTableType
    public CreateCarRequest defineCreateCarRequestEntry(Map<String, String> entry) {
        return new CreateCarRequest(
                entry.get("brand"),
                entry.get("color"),
                entry.get("licensePlate")
        );
    }

    @Given("the car does not already exist with license plate {string}")
    @Transactional
    public void carDoesNotAlreadyExist(String licensePlate) {
        if (carRepository.existsByLicensePlate(licensePlate)) {
            carRepository.deleteCarByLicensePlate(licensePlate);
        }
    }

    @When("I create a car with brand {string}, color {string}, license plate {string}")
    public void createCar(String brand, String color, String licensePlate) {
        CreateCarRequest = new CreateCarRequest(brand, color, licensePlate);
        try {
            carService.createCar(CreateCarRequest).block();
        } catch (DuplicateResourceException e) {
            System.out.println("Car with license plate " + licensePlate + " already exists.");
        }
    }

    @Then("the car with license plate {string} should be created successfully")
    public void carShouldBeCreated(String licensePlate) {
        Car car = carRepository.findByLicensePlateAndIsDeletedFalse(licensePlate).orElse(null);
        Assertions.assertNotNull(car, "Car should be created");
        Assertions.assertEquals(licensePlate, car.getLicensePlate(), "License plates should match");
    }

    @Then("I should retrieve the car with license plate {string}")
    public void shouldRetrieveCar(String licensePlate) {
        Car car = carRepository.findByLicensePlateAndIsDeletedFalse(licensePlate).orElse(null);
        Assertions.assertNotNull(car, "Car should be retrieved");
        Assertions.assertEquals(licensePlate, car.getLicensePlate(), "License plates should match");
    }

    @Then("the car's brand should be {string}")
    public void verifyCarBrand(String brand) {
        Car car = carRepository.findByLicensePlateAndIsDeletedFalse("ABC123").orElse(null);
        Assertions.assertNotNull(car, "Car should not be null");
        Assertions.assertEquals(brand, car.getBrand(), "Brands should match");
    }

    @Then("the car's color should be {string}")
    public void verifyCarColor(String color) {
        Car car = carRepository.findByLicensePlateAndIsDeletedFalse("ABC123").orElse(null);
        Assertions.assertNotNull(car, "Car should not be null");
        Assertions.assertEquals(color, car.getColor(), "Colors should match");
    }

    @When("I try to retrieve a car with non-existent id {long}")
    public void tryToRetrieveCarWithNonExistentId(Long id) {
        try {
            carService.getCarById(id).block();
        } catch (ResourceNotFoundException e) {
            nonExistentCarIdErrorMessage = e.getMessage();
        }
    }

    @Then("an error should be returned indicating the car is not found")
    public void verifyCarNotFoundError() {
        Assertions.assertNotNull(nonExistentCarIdErrorMessage, "Error message should not be null");
        Assertions.assertTrue(nonExistentCarIdErrorMessage.contains("Car not found"), "Error message should indicate the car is not found");
    }

    @When("I delete the car with license plate {string}")
    public void deleteCar(String licensePlate) {
        Car car = carRepository.findByLicensePlateAndIsDeletedFalse(licensePlate).orElseThrow(
                () -> new RuntimeException("Car not found for deletion.")
        );
        carService.deleteCar(car.getId()).block();
    }

    @Then("the car with license plate {string} should not exist")
    public void carShouldNotExist(String licensePlate) {
        Car car = carRepository.findByLicensePlateAndIsDeletedFalse(licensePlate).orElse(null);
        Assertions.assertNull(car, "Car should be deleted and not exist in the database");
    }

    @When("I update the car with license plate {string} to brand {string}, color {string}")
    public void updateCarWithLicensePlate(String licensePlate, String newBrand, String newColor) {
        Car car = carRepository.findByLicensePlateAndIsDeletedFalse(licensePlate)
                .orElseThrow(() -> new RuntimeException("Car not found for update."));

        CreateCarRequest = new CreateCarRequest(newBrand, newColor, car.getLicensePlate());
        carService.updateCar(car.getId(), CreateCarRequest).block();
    }

    @Then("the car's brand should be updated to {string}")
    public void verifyUpdatedCarBrand(String newBrand) {
        Car car = carRepository.findByLicensePlateAndIsDeletedFalse("ABC123").orElse(null);
        Assertions.assertNotNull(car, "Car should not be null");
        Assertions.assertEquals(newBrand, car.getBrand(), "Brands should match");
    }

    @Then("the car's color should be updated to {string}")
    public void verifyUpdatedCarColor(String newColor) {
        Car car = carRepository.findByLicensePlateAndIsDeletedFalse("ABC123").orElse(null);
        Assertions.assertNotNull(car, "Car should not be null");
        Assertions.assertEquals(newColor, car.getColor(), "Colors should match");
    }

    @When("I try to create a car with duplicate license plate {string}")
    public void createCarWithDuplicateLicensePlate(String licensePlate) {
        try {
            carService.createCar(new CreateCarRequest("Brand", "Color", licensePlate)).block();
        } catch (DuplicateResourceException e) {
            errorMessage = e.getMessage();
        }
    }

    @Then("an error should be returned indicating the license plate is already in use")
    public void verifyDuplicateLicensePlateError() {
        Assertions.assertNotNull(errorMessage, "Error message should not be null");
        Assertions.assertTrue(errorMessage.contains("Car with license plate"), "Error message should contain license plate duplication info");
    }

    @Then("the response should contain {int} cars")
    public void thenTheResponseShouldContainCars(int expectedSize) {
        Assertions.assertEquals(expectedSize, carResponses.size(), "Number of cars should match expected count");
    }

    @When("I retrieve cars with brand {string}, color {string}, license plate {string}, page {int}, size {int}")
    public void retrieveCarsWithFilters(String brand, String color, String licensePlate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CarResponse> carPage = carService.getAllCars(pageable, brand, color, licensePlate, true).block();
        carResponses = carPage != null ? carPage.getContent() : List.of();
    }

    @Given("the following cars exist:")
    @Transactional
    public void givenTheFollowingCarsExist(List<CreateCarRequest> cars) {
        for (CreateCarRequest request : cars) {
            if (!carRepository.existsByLicensePlate(request.licensePlate())) {
                carService.createCar(request).block();
            }
        }
    }

    @Then("the first car's color should be {string}")
    public void theFirstCarsBrandShouldBe(String expectedColor) {
        Assertions.assertFalse(carResponses.isEmpty(), "The list of cars should not be empty");
        Assertions.assertEquals(expectedColor, carResponses.get(0).color(), "Color should match");
    }

    @Then("the second car's color should be {string}")
    public void theSecondCarsBrandShouldBe(String expectedColor) {
        Assertions.assertFalse(carResponses.isEmpty(), "The list of cars should not be empty");
        Assertions.assertEquals(expectedColor, carResponses.get(1).color(), "Color should match");
    }

}
