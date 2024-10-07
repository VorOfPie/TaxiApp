Feature: Passenger Service Operations

  Scenario: Create a new passenger
    Given the passenger does not already exist with email "sasha.kamenb@example.com"
    When I create a passenger with first name "Sasha", last name "Kamenb", email "sasha.kamenb@example.com", phone "123456789"
    Then the passenger with email "sasha.kamenb@example.com" should be created successfully

  Scenario: Create a passenger and then try to create a duplicate
    Given the passenger does not already exist with email "sasha.kamenb@example.com"
    When I create a passenger with first name "Sasha", last name "Kamenb", email "sasha.kamenb@example.com", phone "1234567890"
    Then the passenger with email "sasha.kamenb@example.com" should be created successfully
    When I try to create a passenger with duplicate email "sasha.kamenb@example.com"
    Then an error should be returned indicating the email is already in use

  Scenario: Retrieve an existing passenger by email
    Given the passenger does not already exist with email "sasha.kamenb@example.com"
    When the passenger does not already exist with email "sasha.kamenb@example.com"
    And I create a passenger with first name "Sasha", last name "Kamenb", email "sasha.kamenb@example.com", phone "123456789"
    Then I should retrieve the passenger with email "sasha.kamenb@example.com"
    And the passenger's first name should be "Sasha"
    And the passenger's last name should be "Kamenb"

  Scenario: Retrieve a passenger with a non-existent ID
    When I try to retrieve a passenger with non-existent id 99999
    Then an error should be returned indicating the passenger is not found


  Scenario: Create and delete a passenger
    Given the passenger does not already exist with email "sasha.kamenb@example.com"
    When I create a passenger with first name "Sasha", last name "Kamenb", email "sasha.kamenb@example.com", phone "1234567890"
    Then the passenger with email "sasha.kamenb@example.com" should be created successfully
    When I delete the passenger with email "sasha.kamenb@example.com"
    Then the passenger with email "sasha.kamenb@example.com" should not exist

  Scenario: Create, update, and verify a passenger
    Given the passenger does not already exist with email "sasha.kamenb@example.com"
    When I create a passenger with first name "Sasha", last name "Kamenb", email "sasha.kamenb@example.com", phone "1234567890"
    Then the passenger with email "sasha.kamenb@example.com" should be created successfully
    When I update the passenger with email "sasha.kamenb@example.com" to first name "Alex", last name "Kamenb", phone "9876543210"
    Then the passenger's first name should be updated to "Alex"
    And the passenger's last name should be updated to "Kamenb"

  Scenario: Retrieve passengers with filters and pagination
    Given the following passengers exist:
      | firstName | lastName | email                  | phone          |
      | John      | Doe      | john.doe@example.com   | 1234567890     |
      | Jane      | Smith    | jane.smith@example.com | 0987654321     |
      | John      | Adams    | john.adams@example.com | 1122334455     |
    When I retrieve passengers with first name "John", last name "", email "", page 0, size 10
    Then the response should contain 2 passengers
    And the first passenger's first name should be "John"
    And the first passenger's last name should be "Doe"

