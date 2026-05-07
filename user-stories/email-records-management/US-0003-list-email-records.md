# US-0003 - List Email Records

## Metadata
- ID: US-0003
- Status: Approved
- Created: 2026-05-07
- Updated: 2026-05-07
- Author Agent: Product Owner
- Functionality: email-records-management
- Covered Requirements: REQ-0001, REQ-0002, REQ-0003
- Related IDs: None
- Source Links: requirements/REQ-0001-email-crud-api.md, requirements/REQ-0002-authenticated-email-access-and-audit.md, requirements/REQ-0003-email-record-response-fields-contract.md

## User Story
As an authenticated business user,
I want to list all existing email records,
So that I can review and compare all stored contact information with their modification timestamps.

## Scope
- In scope:
  - List all email records accessible to the authenticated user.
  - Restrict list operations to authenticated users.
  - Return public fields (id, value, lastModified) for each record.
  - Enable clients to compare record timestamps for awareness of recency.
- Out of scope:
  - Filtering or search by email value.
  - Sorting or pagination options.
  - Access to deleted records.

## Acceptance Criteria
- [ ] An authenticated user can list all existing email records.
- [ ] An unauthenticated user cannot list email records.
- [ ] Each record in the list includes id, value, and lastModified in response body.
- [ ] Records with different modification times show different lastModified values.
- [ ] The list response reflects the current state of all active records.

## Gherkin Validation Scenarios
```gherkin
Feature: List email records

  Scenario: Authenticated user lists all email records
    Given an authenticated business user has access to email management
    And multiple email records exist
    When the user lists all email records
    Then all email records are returned
    And each record includes id, value, and lastModified
    And records with different modification times show different lastModified values

  Scenario: Unauthenticated user attempts to list email records
    Given a user is not authenticated
    When the user attempts to list email records
    Then the operation is rejected
    And no record data is returned
```

## Functional Analyst Notes
List operations are now a focused user story with a distinct response contract (id/value/lastModified for client-side timestamp awareness). Combined with US-0002 (single read), these provide comprehensive read capabilities.
