# REQ-0002 - Authenticated Email Access and Audit Attribution

## Metadata
- ID: REQ-0002
- Status: Approved
- Created: 2026-05-05
- Updated: 2026-05-05
- Author Agent: Need Collector
- Priority: High
- Related IDs: REQ-0001, REQ-0003
- Source: User request on 2026-05-05: users must be authenticated to track who creates and updates data.

## Business Context
The business requires accountability for data changes so each create and update can be traced back to a verified user identity.

## Requirement Statement
The system shall require authenticated users for email record create, read, update, and delete operations, and shall capture the authenticated actor identity for create and update actions.

## Acceptance Criteria
- [ ] Create, read, update, and delete operations on email records are only allowed for authenticated users.
- [ ] When an email record is created, the actor identity is captured as createdBy.
- [ ] When an email record is updated, the actor identity is captured as updatedBy.

## Dependencies
- Authentication capability that provides a unique user identity for each authenticated request.

## Conflict Check
- Existing requirement checked: REQ-0001
- Conflict found: No
- If Yes, replaces: None
- Replacement rationale: None
- User approval required: No

## Notes
This requirement focuses on access control and audit attribution only.
