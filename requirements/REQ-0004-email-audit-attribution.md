# REQ-0004 - Email Record Audit Attribution

## Metadata
- ID: REQ-0004
- Status: Approved
- Created: 2026-05-06
- Updated: 2026-05-07
- Author Agent: Need Collector
- Priority: High
- Related IDs: REQ-0001, REQ-0002, REQ-0003
- Covered User Stories: US-0001, US-0004
- Source: Split from REQ-0002 on 2026-05-06. Original source: user request on 2026-05-05 — users must be authenticated to track who creates and updates data.

## Business Context
The business requires accountability for data changes so each create and update operation can be traced back to the verified user who performed it.

## Requirement Statement
The system shall capture the authenticated actor identity on each email record create and update operation.

## Acceptance Criteria
- [ ] When an email record is created, the actor identity is captured and stored as createdBy.
- [ ] When an email record is updated, the actor identity is captured and stored as updatedBy.

## Dependencies
- REQ-0002: authentication must provide a unique user identity per request for attribution to be possible.

## Conflict Check
- Existing requirement checked: REQ-0002
- Conflict found: No
- If Yes, replaces: None
- Replacement rationale: This requirement was split out from REQ-0002 to isolate the auditability concern from the access control concern.
- User approval required: No

## Notes
This requirement defines when and what identity data is captured. The fields createdBy and updatedBy are part of the response contract defined in REQ-0003.
