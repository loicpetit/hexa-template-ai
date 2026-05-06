# Traceability Matrix

> Last updated: 2026-05-06  
> Status: Active — 4 Approved requirements, 4 Approved user stories.

## Matrix

| REQ ID | Title | Status | US ID(s) | Feature Guide | TREQ ID(s) | E2E ID(s) |
|--------|-------|--------|----------|----------------|------------|-----------|
| REQ-0001 | Email Records CRUD Lifecycle | Approved | US-0001, US-0002, US-0003, US-0004 | — | — | — |
| REQ-0002 | Authenticated Email Access | Approved | US-0001, US-0002, US-0003, US-0004 | — | — | — |
| REQ-0003 | Email Record Response Fields Contract | Approved | US-0001, US-0002, US-0003 | — | — | — |
| REQ-0004 | Email Record Audit Attribution | Approved | US-0001, US-0003 | — | — | — |

## Gate Status

| Gate | Condition | Status |
|------|-----------|--------|
| Gate 1 | All REQs Approved before US creation | ✅ Completed |
| Gate 2 | All US Approved with feature guides documented before TREQ creation | 🔄 In Progress (US Approved, feature guides pending) |
| Gate 3 | All TREQ (technical choices) Approved before implementation | ⬜ Not started |
| Gate 4 | All E2E tests Pass before feature closure | ⬜ Not started |

## Coverage Rules
- Every **Approved** REQ must be linked to at least one US.
- Every **Approved** US must be linked to a feature guide and at least one TREQ and one E2E test case.
- Feature guides must be created for all approved functionalities and must include real-world examples and how-to guides.
- Missing links block progression to the next gate.

## Artifact Index

### Requirements (`requirements/`)
- REQ-0001 - Email Records CRUD Lifecycle
- REQ-0002 - Authenticated Email Access
- REQ-0003 - Email Record Response Fields Contract
- REQ-0004 - Email Record Audit Attribution

### User Stories (`user-stories/`)
- US-0001 - Create Email Record
- US-0002 - Read Email Records
- US-0003 - Update Email Record
- US-0004 - Hard-Delete Email Record

### Feature Guides (`docs/features/`)
_None yet._

### Technical Requirements (`technical-requirements/`)
_None yet._

### E2E Test Cases (`e2e/`)
_None yet._
