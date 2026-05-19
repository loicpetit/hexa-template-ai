# Traceability Matrix

> Last updated: 2026-05-19  
> Status: Gate 3 Completed — Gate 4 In Progress. Implementation and automated integration tests have started for US-0001. Formal Tester-owned E2E artifacts and feature guides are still pending for feature closure.

## Recent Changes
- **2026-05-19 — Increment 8 Implemented (US-0001 POST /api/emails E2E Integration Test)**
  - **Change**: Implemented `@SpringBootTest` integration coverage for create email flow with real wiring (controller + use case + in-memory repository + API key authentication + audit logger).
  - **Scenarios covered in code**: valid API key (201), missing API key (401), blank value (400).
  - **Traceability Impact**: US-0001 now has implementation-level integration evidence in code. Formal Tester-owned E2E artifact file in `e2e/` is still pending.
  - **Gate Status**: Gate 4 moved to In Progress.

- **2026-05-07 — Gate 3 Completed: All Technical Requirements Approved**
  - **Change**: All 11 TREQs finalized and approved. Final technology selections: Java + Spring Boot (TREQ-0007), In-Memory Singleton Store (TREQ-0006), API Key auth (TREQ-0002), SLF4J + Logback (TREQ-0009), unified HTTP error schema (TREQ-0010), If-Unmodified-Since concurrency control (TREQ-0011), UTC date format standard (TREQ-0012). TREQ-0008 (ORM) deprecated — in-memory store requires no ORM.
  - **Traceability Impact**: Architecture overview updated to reflect all approved decisions; all US→TREQ links confirmed complete.
  - **Gate Status**: Gate 3 ✅ Completed. Developer agent may now begin implementation.
- **2026-05-07 — User Story Refactoring: Split Read Operations into Single-Record and List**
  - **Change**: Original US-0002 (Read Email Records) split into two focused stories:
    - US-0002: Read Single Email Record (response: id, value only; header: Last-Modified)
    - US-0003: List Email Records (response: id, value, lastModified per record)
  - Prior stories renumbered: Update (US-0003 → US-0004), Delete (US-0004 → US-0005)
  - **Reason**: Distinct response contracts justify separate, independently testable user stories; clearer acceptance criteria and Gherkin scenarios per operation
  - **Affected Artifacts**: All 5 US files (split + renumbered); all REQ metadata (added Covered User Stories field, updated references); TREQ-0004 (updated source US references)
  - **Traceability Impact**: Matrix updated; all REQ→US links preserved and refined; no broken references
  - **Gate Status**: Gate 2 remains Completed; Gate 3 continues with updated US count (5 instead of 4)
- **2026-05-07 — API Contract Finalized: Single-Record vs List Response Contracts**
  - **Change**: API response contract refined — single-record operations (create, read by id, update) return only id/value in body; list operations include lastModified field per record; internal metadata stays internal
  - **Reason**: Single-record responses stay minimal for consistency; list responses include lastModified for client-side timestamp awareness; header/field naming consistency (Last-Modified ↔ lastModified)
  - **Affected Artifacts**: REQ-0003, US-0001, US-0002, US-0003, US-0004 (Acceptance Criteria & Gherkin updated); TREQ-0004 (response schema includes separate DTOs)
  - **Traceability Impact**: No links broken; all US still trace correctly to TREQ-0004 and REQ-0003

## Matrix

| REQ ID | Title | Status | US ID(s) | Feature Guide | TREQ ID(s) | E2E ID(s) |
|--------|-------|--------|----------|----------------|------------|-----------|
| REQ-0001 | Email Records CRUD Lifecycle | Approved | US-0001, US-0002, US-0003, US-0004, US-0005 | — | TREQ-0004, TREQ-0006 | — |
| REQ-0002 | Authenticated Email Access | Approved | US-0001, US-0002, US-0003, US-0004, US-0005 | — | TREQ-0002 | — |
| REQ-0003 | Email Record Response Fields Contract | Approved | US-0001, US-0002, US-0003, US-0004 | — | TREQ-0003, TREQ-0004 | — |
| REQ-0004 | Email Record Audit Attribution | Approved | US-0001, US-0004 | — | TREQ-0005 | — |

## Gate Status

| Gate | Condition | Status |
|------|-----------|--------|
| Gate 1 | All REQs Approved before US creation | ✅ Completed |
| Gate 2 | All US Approved before TREQ creation | ✅ Completed |
| Gate 3 | All TREQ (technical choices) Approved before implementation | ✅ Completed — 11 Approved (TREQ-0001–TREQ-0007, TREQ-0009, TREQ-0010, TREQ-0011, TREQ-0012), 1 Deprecated (TREQ-0008 — no ORM needed; in-memory store) |
| Gate 4 | All E2E tests Pass and feature guides documented before feature closure | 🔄 In progress — implementation and integration tests started; formal Tester E2E artifacts and feature guides pending |

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
- US-0002 - Read Single Email Record
- US-0003 - List Email Records
- US-0004 - Update Email Record
- US-0005 - Hard-Delete Email Record

### Feature Guides (`docs/features/`)
_None yet._

### Technical Requirements (`technical-requirements/`)

**Core Architecture TREQs:**
- TREQ-0001 - Hexagonal Architecture Module Organization (Approved ✓)
- TREQ-0002 - Authentication & Authorization Architecture (Approved ✓) — API Key selected for POC
- TREQ-0003 - Email Record Domain Model (Approved ✓) — Rich Domain Entity selected
- TREQ-0004 - Email Record CRUD API Endpoints (Approved ✓) — REST API with If-Unmodified-Since concurrency control
- TREQ-0005 - Audit Attribution System (Approved ✓) — Audit in Application Layer selected

**Infrastructure & Cross-Cutting TREQs:**
- TREQ-0006 - Data Persistence Strategy (Approved ✓) — In-Memory Singleton Store selected
- TREQ-0007 - Technology Stack: Language, Runtime & Framework (Approved ✓) — Java + Spring Boot selected
- TREQ-0008 - Data Access Layer: ORM & Query Builder (Deprecated — in-memory storage selected; no ORM needed)
- TREQ-0009 - Logging & Observability Framework (Approved ✓) — SLF4J + Logback selected
- TREQ-0010 - HTTP Error Response Schema (Approved ✓) — Unified error schema across all endpoints
- TREQ-0011 - Optimistic Concurrency Control via If-Unmodified-Since (Approved ✓) — For update operations
- TREQ-0012 - UTC Date Format Standard (Approved ✓) — All timestamps in UTC (ISO 8601 JSON, RFC 7231 headers)

### E2E Test Cases (`e2e/`)
_None yet._
