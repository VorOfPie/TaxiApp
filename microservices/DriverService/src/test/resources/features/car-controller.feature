Feature: Car Management

  Background:
    Given the car database is empty

  Scenario: Successfully create a new car
    Given the car does not already exist with license plate "ABC123"
    When I create a car with model "Toyota", color "Red", and license plate "ABC123"
    Then the car with license plate "ABC123" should be created successfully

  Scenario: Prevent creating a car with a duplicate license plate
    Given the car does not already exist with license plate "XYZ789"
    When I create a car with model "Honda", color "Blue", and license plate "XYZ789"
    And I try to create a car with duplicate license plate "XYZ789"
    Then an error should be returned indicating the license plate is already in use

  Scenario: Get an existing car by ID
    Given the car with license plate "ABC123" exists to get
    When I get the car with ID
    Then the response status after car creation should be 200
    And the car's brand should be "Toyota"
    And the car's color should be "White"
    And the car's license plate should be "ABC123"

  Scenario: Get a non-existing car by ID
    When I try to get a car with ID 9999
    Then the response status after car creation should be 404

  Scenario: Update an existing car
    Given the car with license plate "ABC123" exists
    When I update the car with model "Honda", color "Black", and license plate "DEF456"
    Then the car with license plate "DEF456" should be updated successfully
    And the updated car should have model "Honda"
    And the updated car should have color "Black"

  Scenario: Update a non-existing car
    When I try to update a car with id 9999 and model "Honda", color "Black", and license plate "DEF456"
    Then an error should be returned indicating the car does not exist for update

  Scenario: Delete an existing car
    Given the car with license plate "ABC123" exists to deletion
    When I delete the car with license plate "ABC123"
    Then the car with license plate "ABC123" should be deleted successfully

  Scenario: Delete a non-existing car
    When I try to delete a car with id 9999
    Then an error should be returned indicating the car does not exist for delete

  Scenario: Filter cars by brand with pagination
    Given the following cars exist:
      | brand  | color | licensePlate |
      | Toyota | White | ABC123       |
      | Honda  | Black | DEF456       |
      | Toyota | Red   | GHI789       |
    When I get cars filtered by brand "Toyota" with page 0 and size 2
    Then the response should contain 2 cars with brand "Toyota"
    And the current car page should be 0, total items 2, total pages 1