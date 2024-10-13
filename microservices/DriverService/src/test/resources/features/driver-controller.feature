Feature: Driver Management

  Background:
    Given the driver and car databases are empty

  Scenario: Successfully create a new driver
    Given I do not have any existing driver with phone number "+1234567890"
    When I create a driver with first name "John", last name "Doe", phone number "+1234567890", gender "Male"
    Then the driver with phone number "+1234567890" should be created successfully

  Scenario: Prevent creating a driver with an invalid phone number
    When I create a driver with invalid phone and first name "Jane", last name "Doe", phone number "invalid-phone", gender "Female"
    Then an error should be returned indicating the phone number is invalid

  Scenario: Get an existing driver by ID
    Given the driver with phone number "+1234567890" exists to get
    When I get the driver with ID
    Then the response status after driver creation should be 200
    And the driver's first name should be "John"
    And the driver's last name should be "Doe"
    And the driver's phone number should be "+1234567890"
    And the driver's gender should be "Male"

  Scenario: Get a non-existing driver by ID
    When I try to get a driver with ID 9999
    Then the response status after driver creation should be 404

  Scenario: Update an existing driver
    Given the driver with phone number "+1234567890" exists
    When I update the driver with first name "Jane", last name "Doe", phone number "+9876543210", gender "Female", and car license plate "DEF456"
    Then the driver with phone number "+9876543210" should be updated successfully
    And the updated driver should have first name "Jane"
    And the updated driver should have last name "Doe"
    And the updated driver should have gender "Female"
    And the updated driver should have car license plate "DEF456"

  Scenario: Update a non-existing driver
    When I try to update a driver with id 9999, first name "Jane", last name "Doe", phone number "+9876543210", gender "Female", and car license plate "DEF456"
    Then an error should be returned indicating the driver does not exist for update

  Scenario: Delete an existing driver
    Given the driver with phone number "+1234567890" exists for deletion
    When I delete the driver with phone number "+1234567890"
    Then the driver with phone number "+1234567890" should be deleted successfully

  Scenario: Delete a non-existing driver
    When I try to delete a driver with id 9999
    Then an error should be returned indicating the driver does not exist for delete

  Scenario: Filter drivers by last name with pagination
    Given the following drivers exist:
      | firstName | lastName | phone       | gender | cars                      |
      | John      | Doe      | +1234567890 | Male   | Toyota Camry ABC123       |
      | Jane      | Doe      | +7876543210 | Female | NotToyota NotCamry CBA123 |
      | Jack      | Smith    | +1122334455 | Male   | Toyota Camry ACB123       |
    When I get drivers filtered by last name "Doe" with page 0 and size 2
    Then the response should contain 2 drivers with last name "Doe"
    And the current driver page should be 0, total items 2, total pages 1
