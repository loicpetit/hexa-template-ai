# US-0001 - Create Email Record

## Metadata
- ID: US-0001
- Status: Approved
- Created: 2026-05-06
- Updated: 2026-05-06
- Author Agent: Product Owner
- Functionality: email-records-management
- Covered Requirements: REQ-0001, REQ-0002, REQ-0003, REQ-0004
- Related IDs: None
- Source Links: requirements/REQ-0001-email-crud-api.md, requirements/REQ-0002-authenticated-email-access-and-audit.md, requirements/REQ-0003-email-record-response-fields-contract.md, requirements/REQ-0004-email-audit-attribution.md

## User Story
As an authenticated business user,
I want to create a new email record,
So that I can register and track a new communication address in the business system.

## Scope
- In scope:
  - Create a new email record when mandatory information is provided.
  - Restrict creation to authenticated users.
  - Return a created record with complete business fields.
  - Capture who created the record.
- Out of scope:
  - Bulk creation of email records.
  - Advanced data validation rules beyond required business fields.

## Acceptance Criteria
- [ ] An authenticated user can create an email record.
- [ ] An unauthenticated user cannot create an email record.
- [ ] The created record returned to the user includes id, value, created, createdBy, updated, and updatedBy.
- [ ] The creator identity is recorded in createdBy.

## Gherkin Validation Scenarios
```gherkin
Feature: Create email record

  Scenario: Authenticated user creates an email record
    Given an authenticated business user is on the email management workflow
    When the user creates an email record with valid information
    Then a new email record is stored
    And the returned record includes id, value, created, createdBy, updated, and updatedBy
    And createdBy identifies the authenticated user

  Scenario: Unauthenticated user attempts to create an email record
    Given a user is not authenticated
    When the user attempts to create an email record
    Then the operation is rejected
    And no email record is created
```

## Functional Analyst Notes
Create operation is intentionally isolated from update and delete to respect the requested sub-operation independence for REQ-0001.
