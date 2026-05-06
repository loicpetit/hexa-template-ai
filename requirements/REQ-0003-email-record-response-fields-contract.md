# REQ-0003 - Email Record Response Fields Contract

## Metadata
- ID: REQ-0003
- Status: Approved
- Created: 2026-05-05
- Updated: 2026-05-06
- Author Agent: Need Collector
- Priority: High
- Related IDs: REQ-0001, REQ-0002, REQ-0004
- Source: User request on 2026-05-05: responses must include id, value, created, createdBy, updated, updatedBy.

## Business Context
The business needs consistent API responses so clients can reliably consume email data and display audit information.

## Requirement Statement
The system shall return, for create, read, update, and list operations, email records that include id, value, created, createdBy, updated, and updatedBy.

## Acceptance Criteria
- [ ] For create operations, the returned email record includes id, value, created, createdBy, updated, and updatedBy.
- [ ] For read operations, the returned email record includes id, value, created, createdBy, updated, and updatedBy.
- [ ] For update operations, the returned email record includes id, value, created, createdBy, updated, and updatedBy.
- [ ] For list operations, each returned email record includes id, value, created, createdBy, updated, and updatedBy.

## Dependencies
- REQ-0001 for CRUD lifecycle support.
- REQ-0004 for attribution semantics of createdBy and updatedBy fields.

## Conflict Check
- Existing requirement checked: REQ-0001
- Conflict found: No
- If Yes, replaces: None
- Replacement rationale: None
- User approval required: No

## Notes
This requirement defines required returned fields only and is implementation-format neutral.
Product Owner first-pass feedback: Changes required (remove format-specific wording and keep field contract only), applied on 2026-05-06.