Feature: Car Service Operations

  Scenario: Create a new car
    Given the car does not already exist with license plate "ABC123"
    When I create a car with brand "Toyota", color "Red", license plate "ABC123"
    Then the car with license plate "ABC123" should be created successfully

  Scenario: Create a car and then try to create a duplicate
    Given the car does not already exist with license plate "ABC123"
    When I create a car with brand "Toyota", color "Red", license plate "ABC123"
    Then the car with license plate "ABC123" should be created successfully
    When I try to create a car with duplicate license plate "ABC123"
    Then an error should be returned indicating the license plate is already in use

  Scenario: Retrieve an existing car by license plate
    Given the car does not already exist with license plate "ABC123"
    When I create a car with brand "Toyota", color "Red", license plate "ABC123"
    Then I should retrieve the car with license plate "ABC123"
    And the car's brand should be "Toyota"
    And the car's color should be "Red"

  Scenario: Retrieve a car with a non-existent ID
    When I try to retrieve a car with non-existent id 99999
    Then an error should be returned indicating the car is not found

  Scenario: Create and delete a car
    Given the car does not already exist with license plate "ABC123"
    When I create a car with brand "Toyota", color "Red", license plate "ABC123"
    Then the car with license plate "ABC123" should be created successfully
    When I delete the car with license plate "ABC123"
    Then the car with license plate "ABC123" should not exist

  Scenario: Create, update, and verify a car
    Given the car does not already exist with license plate "ABC123"
    When I create a car with brand "Toyota", color "Red", license plate "ABC123"
    Then the car with license plate "ABC123" should be created successfully
    When I update the car with license plate "ABC123" to brand "Honda", color "Blue"
    Then the car's brand should be updated to "Honda"
    And the car's color should be updated to "Blue"

  Scenario: Retrieve cars with filters and pagination
    Given the following cars exist:
      | brand | color | licensePlate|
      | Toyota| Red   | 123ABC      |
      | Honda | Blue  | 456DEF      |
      | Toyota| Black | 789CBA      |
    When I retrieve cars with brand "Toyota", color "", license plate "", page 0, size 10
    Then the response should contain 2 cars
    And the first car's color should be "Red"
    And the second car's color should be "Black"
