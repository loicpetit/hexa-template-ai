# Hexa Template AI — Hexagonal Architecture with Full Traceability

## Purpose
Deliver robust web application features with full traceability from business need to test evidence using specialized AI agents and hexagonal architecture.

## Current Status

### Gate 1 ✅ Completed
- **4 Approved Requirements**:
  - REQ-0001: Email Records CRUD Lifecycle
  - REQ-0002: Authenticated Email Access
  - REQ-0003: Email Record Response Fields Contract
  - REQ-0004: Email Record Audit Attribution

### Gate 2 ✅ Completed
- **4 Approved User Stories**:
  - US-0001: Create Email Record
  - US-0002: Read Email Records
  - US-0003: Update Email Record
  - US-0004: Hard-Delete Email Record
- All REQs are now covered by user stories
- All US have Gherkin validation scenarios

### Gate 3 ⬜ Not Started
- Technical Requirements (TREQ-XXXX) to be created by Software Architect
- Architecture documentation to be aligned with approved TREQs

### Gate 4 ⬜ Not Started
- E2E test cases to be created and executed by Tester
- Feature guides to be created by Product Owner after implementation and testing

## Key Workflow Features

### Atomic Requirements
Requirements are split to isolate independent concerns:
- **REQ-0002** isolated to access control only
- **REQ-0004** split out for audit attribution
- **REQ-0003** contains only required fields (implementation-format neutral, no JSON technical detail)

### Quality Gates
- Need Collector now obtains Product Owner first-pass feedback before finalizing REQs
- Feature guides created post-implementation (Gate 4) so Product Owner can see real application behavior
- Full traceability matrix maintained at each phase

## Project Structure
```
requirements/                  ← REQ-XXXX files (Need Collector)
user-stories/                  ← US-XXXX files grouped by functionality (Product Owner)
technical-requirements/        ← TREQ-XXXX files (Software Architect)
e2e/                           ← E2E-XXXX test cases (Tester)
docs/
  architecture/                ← Architecture documentation (Software Architect)
  dev/                         ← Implementation notes (Developer)
  features/                    ← Feature user guides (Product Owner, post-Gate 4)
traceability.md                ← Live traceability matrix (all agents)
```

## Approved Agent Workflows
1. **Need Collector** → Creates atomic REQs with Product Owner first-pass feedback
2. **Product Owner** → Converts REQs to US (Gate 2) with Developer Gherkin validation, creates feature guides post-implementation (Gate 4)
3. **Software Architect** → Creates TREQs from approved US with Developer implementability validation (Gate 3)
4. **Developer** → Reviews Gherkin scenarios for testability (Gate 2), reviews TREQs for implementability (Gate 3), implements features per approved TREQs (Gate 4)
5. **Tester** → Creates and runs E2E tests, validates feature behavior (Gate 4)
