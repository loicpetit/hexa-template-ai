# US-0003 - Update Email Record

## Metadata
- ID: US-0003
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
I want to update an existing email record,
So that stored contact information stays accurate over time.

## Scope
- In scope:
  - Update an existing email record.
  - Restrict updates to authenticated users.
  - Return the updated record with complete business fields.
  - Capture who updated the record.
- Out of scope:
  - Version history browsing.
  - Batch update of multiple records.

## Acceptance Criteria
- [ ] An authenticated user can update an existing email record.
- [ ] An unauthenticated user cannot update an email record.
- [ ] The updated record returned to the user includes id, value, created, createdBy, updated, and updatedBy.
- [ ] The updater identity is recorded in updatedBy.

## Gherkin Validation Scenarios
```gherkin
Feature: Update email record

  Scenario: Authenticated user updates an email record
    Given an authenticated business user has an existing email record
    When the user updates the email record
    Then the record changes are saved
    And the returned record includes id, value, created, createdBy, updated, and updatedBy
    And updatedBy identifies the authenticated user

  Scenario: Unauthenticated user attempts to update an email record
    Given a user is not authenticated
    When the user attempts to update an email record
    Then the operation is rejected
    And the email record remains unchanged
```

## Functional Analyst Notes
Update operation is separated from create and delete to keep business ownership clear and independently testable.
