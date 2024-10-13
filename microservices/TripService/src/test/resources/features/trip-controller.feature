Feature: Trip Service

  Background:
    Given the trip database is empty

  Scenario: Create trip successfully
    Given a valid trip request with origin "123 Origin St", destination "456 Destination Ave", driver ID 1, passenger ID 2, status "CREATED", and price "50.00"
    When I create a trip
    Then the trip should be created successfully

  Scenario: Invalid trip request
    Given an invalid trip request with null fields
    When I create a trip
    Then I should receive a bad request status after invalid request

  Scenario: Passenger does not exist
    Given a valid trip request with driver ID 1 and non existing passenger ID 999
    When I create a trip
    Then I should receive a bad request status after invalid request

  Scenario: Driver does not exist
    Given a valid trip request with non existing driver ID 999 and passenger ID 2
    When I create a trip
    Then I should receive a bad request status after invalid request

  Scenario: Get trip by ID successfully
    Given a valid trip request with driver ID 1, passenger ID 2, origin "123 Origin St", destination "456 Destination Ave", status "CREATED", and price "50.00"
    When I get the trip by id
    Then the trip details should be returned

  Scenario: Get trip by non-existing ID
    When I get the trip by id
    Then I should receive a not found status for trip

  Scenario: Update trip successfully
    Given a valid trip request for update with driver ID 1, passenger ID 2, origin "123 Origin St", destination "456 Destination Ave", status "CREATED", and price "50.00"
    When I update the trip with new destination "789 Updated Ave" and new price "75.0"
    Then the trip should be updated successfully with destination "789 Updated Ave", status "CREATED", and price "75.0"

  Scenario: Update a non-existing trip
    When I try to update a trip with id 9999 and new destination "789 Updated Ave", new price "75.0"
    Then an error should be returned indicating the trip does not exist for update

  Scenario: Delete an existing trip
    Given the trip already exists for deletion
    When I delete the trip
    Then the trip should be deleted successfully

  Scenario: Delete a non-existing trip
    When I try to delete a trip with id 9999
    Then an error should be returned indicating the trip does not exist for deletion

  Scenario: Filter trips by driverId with pagination
    Given the following trips exist:
      | driverId | passengerId | origin    | destination   | status  | price | orderDateTime       |
      | 1        | 1           | Address 1 | Destination 1 | CREATED | 5.0   | 2023-10-10T10:00:00 |
      | 1        | 2           | Address 2 | Destination 2 | CREATED | 10.0  | 2023-10-10T11:00:00 |
      | 2        | 1           | Address 3 | Destination 3 | CREATED | 0.01  | 2023-10-10T12:00:00 |
    When I get trips filtered by driverId "1" with page 0 and size 10
    Then the response should contain 2 trips
    And the current page should be 0, total items 2, total pages 1
    And the first trip price should be 5.0
    And the second trip price should be 10.0

  Scenario: Close a trip and send a rating message
    Given a trip with driverId 1, passengerId 2, origin "123 Origin St", and destination "456 Destination Ave" exists
    When I close the trip and provide a score of 5.0 with comment "Great trip!"
    Then a rating message should be sent to Kafka with driverId 1, passengerId 2, score 5.0, and comment "Great trip!"