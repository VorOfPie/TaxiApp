Feature: Driver Service Operations

  Scenario: Create a new driver
    Given the driver does not already exist with phone "123456789"
    When I create a driver with first name "John", last name "Doe", phone "123456789", gender "Male"
    Then the driver with phone "123456789" should be created successfully

  Scenario: Create a driver and then try to create a duplicate
    Given the driver does not already exist with phone "123456789"
    When I create a driver with first name "John", last name "Doe", phone "123456789", gender "Male"
    Then the driver with phone "123456789" should be created successfully
    When I try to create a driver with duplicate phone "123456789"
    Then an error should be returned indicating the phone is already in use

  Scenario: Retrieve an existing driver by phone
    Given the driver does not already exist with phone "123456789"
    When the driver does not already exist with phone "123456789"
    And I create a driver with first name "John", last name "Doe", phone "123456789", gender "Male"
    Then I should retrieve the driver with phone "123456789"
    And the driver's first name should be "John"
    And the driver's last name should be "Doe"

  Scenario: Retrieve a driver with a non-existent ID
    When I try to retrieve a driver with non-existent id 99999
    Then an error should be returned indicating the driver is not found

  Scenario: Create and delete a driver
    Given the driver does not already exist with phone "123456789"
    When I create a driver with first name "John", last name "Doe", phone "123456789", gender "Male"
    Then the driver with phone "123456789" should be created successfully
    When I delete the driver with phone "123456789"
    Then the driver with phone "123456789" should not exist

  Scenario: Create, update, and verify a driver
    Given the driver does not already exist with phone "123456789"
    When I create a driver with first name "John", last name "Doe", phone "123456789", gender "Male"
    Then the driver with phone "123456789" should be created successfully
    When I update the driver with phone "123456789" to first name "Alex", last name "Doe"
    Then the driver's first name should be updated to "Alex"
    And the driver's last name should be updated to "Doe"

  Scenario: Retrieve drivers with filters and pagination
    Given the following drivers exist:
      | firstName | lastName | phone         | gender |
      | John      | Doe      | 1234567890    | Male   |
      | Jane      | Smith    | 0987654321    | Female |
      | John      | Adams    | 1122334455    | Male   |
    When I retrieve drivers with first name "John", last name "", phone "", page 0, size 10
    Then the response should contain 2 drivers
    And the first driver's first name should be "John"
    And the first driver's last name should be "Doe"
