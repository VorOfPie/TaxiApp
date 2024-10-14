Feature: Passenger Management

  Background:
    Given the passenger database is empty

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

  Scenario: Get an existing passenger by ID
    Given the passenger with email "alice.smith@example.com" exists to get
    When I get the passenger with ID
    Then the response status should be 200
    And the passenger's first name should be "Alice"
    And the passenger's last name should be "Smith"
    And the passenger's email should be "alice.smith@example.com"
    And the passenger's phone should be "+1234567890"

  Scenario: Get a non-existing passenger by ID
    When I try to get a passenger with ID 9999
    Then the response status should be 404

  Scenario: Update an existing passenger
    Given the passenger with email "sasha.kamenb@example.com" exists
    When I update the passenger with first name "Sasha", last name "Kamenb", email "sasha.kamenb@example.com", phone "987654321"
    Then the passenger with email "sasha.kamenb@example.com" should be updated successfully
    And the updated passenger should have phone number "987654321"

  Scenario: Update a non-existing passenger
    When I try to update a passenger with id 9999 and first name "Sasha", last name "Kamenb", email "sasha.kamenb@example.com", phone "987654321"
    Then an error should be returned indicating the passenger does not exist for update

  Scenario: Delete an existing passenger
    Given the passenger with email "john.doe@example.com" exists to deletion
    When I delete the passenger with email "john.doe@example.com"
    Then the passenger with email "john.doe@example.com" should be deleted successfully

  Scenario: Delete a non-existing passenger
    When I try to delete a passenger with id 9999
    Then an error should be returned indicating the passenger does not exist for delete

  Scenario: Filter passengers by first name with pagination
    Given the following passengers exist:
      | firstName | lastName | email                   | phone       |
      | Alice     | Smith    | alice.smith@example.com | +1234567891 |
      | Bob       | Johnson  | bob.johnson@example.com | +1234567892 |
      | Alice     | Brown    | alice.brown@example.com | +1234567893 |
    When I get passengers filtered by first name "Alice" with page 0 and size 2
    Then the response should contain 2 passengers with first name "Alice"
    And the current page should be 0, total items 2, total pages 1