# US-0002 - Read Single Email Record

## Metadata
- ID: US-0002
- Status: Approved
- Created: 2026-05-06
- Updated: 2026-05-07
- Author Agent: Product Owner
- Functionality: email-records-management
- Covered Requirements: REQ-0001, REQ-0002, REQ-0003
- Related IDs: None
- Source Links: requirements/REQ-0001-email-crud-api.md, requirements/REQ-0002-authenticated-email-access-and-audit.md, requirements/REQ-0003-email-record-response-fields-contract.md

## User Story
As an authenticated business user,
I want to read one email record by its identifier,
So that I can retrieve and review a specific stored contact address reliably.

## Scope
- In scope:
  - Read one email record by its identifier.
  - Restrict read operations to authenticated users.
  - Return public fields (id, value) for the record.
  - Include Last-Modified header with timestamp for concurrency awareness.
- Out of scope:
  - Listing multiple records (see US-0003).
  - Filtering or search by email value.
  - Access to deleted records.

## Acceptance Criteria
- [ ] An authenticated user can read one email record by identifier.
- [ ] An unauthenticated user cannot read an email record.
- [ ] The read response returns only id and value in response body.
- [ ] The response includes Last-Modified header with the updated timestamp (RFC 7231 format).

## Gherkin Validation Scenarios
```gherkin
Feature: Read single email record

  Scenario: Authenticated user reads a single email record by identifier
    Given an authenticated business user has access to email management
    And an email record exists with id "550e8400-e29b-41d4-a716-446655440000"
    When the user reads the email record by its identifier
    Then the requested record is returned with only id and value
    And the response includes Last-Modified header with the updated timestamp

  Scenario: Unauthenticated user attempts to read a single email record
    Given a user is not authenticated
    When the user attempts to read an email record by identifier
    Then the operation is rejected
    And no record data is returned
```

## Functional Analyst Notes
Single record read is now a focused user story with a distinct response contract (id/value only, Last-Modified header). Combined with US-0003 (list), these cover all read operations.
