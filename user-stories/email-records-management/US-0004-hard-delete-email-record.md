# US-0004 - Hard-Delete Email Record

## Metadata
- ID: US-0004
- Status: Approved
- Created: 2026-05-06
- Updated: 2026-05-06
- Author Agent: Product Owner
- Functionality: email-records-management
- Covered Requirements: REQ-0001, REQ-0002
- Related IDs: None
- Source Links: requirements/REQ-0001-email-crud-api.md, requirements/REQ-0002-authenticated-email-access-and-audit.md

## User Story
As an authenticated business user,
I want to permanently delete an email record,
So that obsolete or invalid data is fully removed from active business records.

## Scope
- In scope:
  - Permanently delete an existing email record.
  - Restrict deletion to authenticated users.
  - Ensure deleted records are no longer retrievable.
- Out of scope:
  - Soft delete or archival behavior.
  - Recovery of deleted records.

## Acceptance Criteria
- [ ] An authenticated user can permanently delete an existing email record.
- [ ] An unauthenticated user cannot delete an email record.
- [ ] Once deleted, the email record is no longer retrievable.

## Gherkin Validation Scenarios
```gherkin
Feature: Hard-delete email record

  Scenario: Authenticated user permanently deletes an email record
    Given an authenticated business user has an existing email record
    When the user permanently deletes the email record
    Then the email record is removed
    And the deleted email record cannot be retrieved afterward

  Scenario: Unauthenticated user attempts to delete an email record
    Given a user is not authenticated
    When the user attempts to delete an email record
    Then the operation is rejected
    And the email record remains available
```

## Functional Analyst Notes
Hard-delete is modeled as an independent user story as requested, with explicit irreversibility in business terms.
