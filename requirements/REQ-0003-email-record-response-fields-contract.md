# REQ-0003 - Email Record Response Fields Contract

## Metadata
- ID: REQ-0003
- Status: Approved
- Created: 2026-05-05
- Updated: 2026-05-07
- Author Agent: Need Collector (clarified by Software Architect for technical alignment on 2026-05-07)
- Priority: High
- Related IDs: REQ-0001, REQ-0002, REQ-0004
- Covered User Stories: US-0001, US-0002, US-0003, US-0004
- Source: User request on 2026-05-05: responses must include id, value, created, createdBy, updated, updatedBy.

## Business Context
The business needs consistent API responses so clients can reliably consume email data and display audit information.

## Requirement Statement
The system shall return, for create, read, and update operations, email records with public fields (id, value) in response body. For list operations, email records shall include public fields (id, value, lastModified) in response body to enable clients to compare record timestamps. Internal metadata (created, createdBy, updated, updatedBy) shall not be exposed in single-record response bodies and shall be used internally for audit purposes only. The updated timestamp shall be exposed via Last-Modified response header (RFC 7231 format) for single-record operations to support optimistic concurrency control via If-Unmodified-Since request header.

## Acceptance Criteria
- [ ] For create operations, the returned email record includes only id and value in response body.
- [ ] For read operations, the returned email record includes only id and value in response body.
- [ ] For update operations, the returned email record includes only id and value in response body.
- [ ] For list operations, each returned email record includes id, value, and lastModified in response body.
- [ ] Single-record operations (create, read, update) include Last-Modified header with the updated timestamp (RFC 7231 format).
- [ ] Internal metadata (created, createdBy, updatedBy) is never exposed in response bodies.
- [ ] lastModified field in list responses corresponds to the Last-Modified header value for single-record operations.

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
This requirement enforces clean API boundaries: public fields (id, value) are always exposed; lastModified field exposed in list responses only (for client-side comparison, semantic match to Last-Modified header); Last-Modified header used for single-record operations (concurrency control). Internal metadata (created, createdBy, updatedBy) stays internal for audit tracking.
Updated 2026-05-07: Clarified that lastModified field in list responses provides consistency with Last-Modified header naming.