Feature: Rating Service

  Background:
    Given the rating database is empty

  Scenario: Create rating successfully
    Given a valid rating request with driver ID 1, passenger ID 2, score 5.0, and comment "Great service"
    When I create a rating
    Then the rating should be created successfully

  Scenario: Invalid rating request
    Given an invalid rating request with null fields
    When I try to create a rating
    Then I should receive a bad request status

  Scenario: Passenger does not exist
    Given a valid rating request with driver ID 1 and non existing passenger ID 999
    When I try to create a rating
    Then I should receive a not found status

  Scenario: Driver does not exist
    Given a valid rating request with non existing driver ID 999 and passenger ID 2
    When I try to create a rating
    Then I should receive a not found status

  Scenario: Get an existing rating by ID
    Given a rating to get exists with driver ID 1, passenger ID 2, score 5, and comment "Great service"
    When I get the rating with ID
    Then the response status should be 200
    And the rating's driver ID should be 1
    And the rating's passenger ID should be 2
    And the rating's score should be 5
    And the rating's comment should be "Great service"

  Scenario: Get a non-existing rating by ID
    When I try to get a rating with ID 999
    Then the response status should be 404

  Scenario: Update an existing rating
    Given a rating to update exists with driver ID 1, passenger ID 2, score 5.0, and comment "Great service"
    When I update the rating with score 4.0 and comment "Good service"
    Then the rating with ID should be updated successfully
    And the updated rating should have a score of 4.0
    And the updated rating should have a comment "Good service"

  Scenario: Update a non-existing rating
    When I try to update a rating with id 999 with score 4.0 and comment "Good service"
    Then an error should be returned indicating the rating does not exist for update


  Scenario: Delete an existing rating
    Given the rating already exists for deletion
    When I delete the rating
    Then the rating should be deleted successfully

  Scenario: Delete a non-existing rating
    When I try to delete a rating with id 9999
    Then an error should be returned indicating the rating does not exist for deletion

  Scenario: Filter ratings by driver ID with pagination
    Given the following ratings exist:
      | driverId | passengerId | score | comment           |
      | 1        | 2           | 5.0   | Excellent service |
      | 1        | 3           | 4.0   | Good service      |
      | 2        | 2           | 3.0   | Average service   |
    When I get ratings filtered by driver ID "1" with page 0, size 10, and sorted by "id,asc"
    Then the response should contain 2 ratings for driver ID "1"
    And the ratings should be sorted in ascending order of their IDs
    And the current page should be 0, total items 2, total pages 1


  Scenario: Save new rating when a new rating event is received
    When a new rating event is received with driver ID 1, passenger ID 2, score 5.0, and comment "Great service"
    Then the rating should be saved in the database for driver ID 1 and passenger ID 2

  Scenario: Do not save duplicate rating when a rating event for the same driver and passenger is received
    Given a rating already exists for driver ID 1 and passenger ID 2
    When a new rating event is received with driver ID 1, passenger ID 2, score 5.0, and comment "Great service"
    Then the rating should not be duplicated in the database
    And the total number of ratings should be 1