# US-0002 - Read Email Records

## Metadata
- ID: US-0002
- Status: Approved
- Created: 2026-05-06
- Updated: 2026-05-06
- Author Agent: Product Owner
- Functionality: email-records-management
- Covered Requirements: REQ-0001, REQ-0002, REQ-0003
- Related IDs: None
- Source Links: requirements/REQ-0001-email-crud-api.md, requirements/REQ-0002-authenticated-email-access-and-audit.md, requirements/REQ-0003-email-record-response-fields-contract.md

## User Story
As an authenticated business user,
I want to read one email record and list existing email records,
So that I can find and review stored contact information reliably.

## Scope
- In scope:
  - Read one email record by its identifier.
  - List existing email records.
  - Restrict read and list operations to authenticated users.
  - Return required business fields for each returned record.
- Out of scope:
  - Filtering and sorting options.
  - Access to deleted records.

## Acceptance Criteria
- [ ] An authenticated user can read one email record by identifier.
- [ ] An authenticated user can list existing email records.
- [ ] An unauthenticated user cannot read or list email records.
- [ ] Each returned record includes only id and value.
- [ ] Responses include Last-Modified header with the updated timestamp (RFC 7231 format).

## Gherkin Validation Scenarios
```gherkin
Feature: Read email records

  Scenario: Authenticated user reads and lists email records
    Given an authenticated business user has access to email management
    And email records exist
    When the user reads one email record by identifier
    And the user lists email records
    Then the requested record and the listed records are returned
    And each returned record includes only id and value
    And responses include Last-Modified header with the updated timestamp

  Scenario: Unauthenticated user attempts to read email records
    Given a user is not authenticated
    When the user attempts to read or list email records
    Then the operation is rejected
    And no record data is returned
```

## Functional Analyst Notes
Read-by-id and list are grouped as one read-focused interaction because they share the same business intent and output contract.
