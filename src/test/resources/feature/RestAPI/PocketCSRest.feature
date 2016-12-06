Feature: PocketCS Rest
    As an API user, I want to check whether I get data from a specific table.

    Scenario: Testing getTable Endpoint with no inputs
        When I call the 'getTable' endpoint with no input
        Then the result of 'getTable' endpoint should be 'missing parameter tableName'

    Scenario: Testing 'getTable' Endpoint with incorrect table name
        When I call the 'getTable' endpoint on the table 'Something'
        Then the result of 'getTable' endpoint should be 'table not found'

    Scenario: Testing 'getTable' Endpoint with correct table name
        When I call the 'getTable' endpoint on the table 'Algorithms'
        Then the result of 'getTable' endpoint should not be empty


    # Testing addUser Rest endpoint
    Scenario: Testing addUser Endpoint with no inputs
        When I call the 'addUsers' endpoint with no input
        Then the result of 'addUser' endpoint should be 'Missing parameters: [facebookID key , firstname , lastname ]'
