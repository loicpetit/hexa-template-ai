# Traceability Matrix

> Last updated: 2026-05-07  
> Status: Gate 3 In Progress — 4 Approved requirements, 4 Approved user stories, 2 Approved technical requirements (TREQ-0001, TREQ-0002), 4 Draft technical requirements.

## Matrix

| REQ ID | Title | Status | US ID(s) | Feature Guide | TREQ ID(s) | E2E ID(s) |
|--------|-------|--------|----------|----------------|------------|-----------|
| REQ-0001 | Email Records CRUD Lifecycle | Approved | US-0001, US-0002, US-0003, US-0004 | — | TREQ-0004, TREQ-0006 | — |
| REQ-0002 | Authenticated Email Access | Approved | US-0001, US-0002, US-0003, US-0004 | — | TREQ-0002 | — |
| REQ-0003 | Email Record Response Fields Contract | Approved | US-0001, US-0002, US-0003 | — | TREQ-0003, TREQ-0004 | — |
| REQ-0004 | Email Record Audit Attribution | Approved | US-0001, US-0003 | — | TREQ-0005 | — |

## Gate Status

| Gate | Condition | Status |
|------|-----------|--------|
| Gate 1 | All REQs Approved before US creation | ✅ Completed |
| Gate 2 | All US Approved before TREQ creation | ✅ Completed |
| Gate 3 | All TREQ (technical choices) Approved before implementation | 🔄 In Progress — 2 Approved (TREQ-0001, TREQ-0002), 4 Draft (awaiting database selection + Developer review) |
| Gate 4 | All E2E tests Pass and feature guides documented before feature closure | ⬜ Not started |

## Coverage Rules
- Every **Approved** REQ must be linked to at least one US (Gate 2).
- Every **Approved** US must be linked to at least one TREQ and one E2E test case (Gate 3 & 4).
- Feature guides are created after implementation and testing complete (Gate 4), not before.
- Feature guides must include real-world examples, how-to guides, and step-by-step instructions.
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
- TREQ-0001 - Hexagonal Architecture Module Organization (Approved ✓)
- TREQ-0002 - Authentication & Authorization Architecture (Approved ✓) — API Key selected for POC
- TREQ-0003 - Email Record Domain Model (Draft)
- TREQ-0004 - Email Record CRUD API Endpoints (Draft)
- TREQ-0005 - Audit Attribution System (Draft)
- TREQ-0006 - Data Persistence Strategy (Draft — awaiting database technology selection)

### E2E Test Cases (`e2e/`)
_None yet._
