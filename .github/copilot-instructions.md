# Hexa Template — Global AI Agent Instructions

## Project Purpose
Deliver robust web application features with full traceability from business need to test evidence, using specialized AI agents and hexagonal architecture.

## Execution Model
- The workflow is **sequential and gated**.
- A next phase cannot start until the previous gate is **explicitly approved by the user**.
- All artifacts are markdown files stored in the repository.

## Folder Structure
```
requirements/                  ← REQ-XXXX files (Need Collector)
user-stories/                  ← US-XXXX files grouped by functionality (Product Owner)
technical-requirements/        ← TREQ-XXXX files (Software Architect)
e2e/                           ← E2E-XXXX test cases grouped by functionality (Tester)
traceability.md                ← Live traceability matrix (all agents update this)
```

## ID Formats
| Artifact | Format | Example |
|----------|--------|---------|
| Business requirement | `REQ-0001` | `REQ-0042` |
| User story | `US-0001` | `US-0007` |
| Technical requirement | `TREQ-0001` | `TREQ-0003` |
| E2E test case | `E2E-0001` | `E2E-0011` |

## Allowed Statuses
`Draft` → `Proposed` → `Approved` → `Replaced` | `Deprecated`

## Mandatory Artifact Fields
Every artifact file must contain:
- ID
- Title
- Status
- Created date
- Updated date
- Author agent
- Source links
- Related IDs

## Naming Rules
- One artifact per file.
- Prefix filename with its ID.
- Example: `requirements/REQ-0001-user-authentication.md`

## Quality Rules
- Requirements and technical requirements must be **atomic and testable**.
- When a requirement mixes multiple concerns (for example behavior, access control, and response contract), split it into separate REQs to improve separation of concerns.
- Any replacement must declare the replaced ID and rationale.
- Any conflict must include impact analysis on linked artifacts.
- No duplicate technical requirement is allowed.
- Traceability must always be **complete and up to date**.

## Gate Rules
| Gate | Condition |
|------|-----------|
| **Gate 1** | All REQs **Approved** before user stories are created |
| **Gate 2** | All US **Approved** before technical requirements are created |
| **Gate 3** | All TREQ (technical choices) **Approved** before implementation starts |
| **Gate 4** | All E2E tests **Pass** and evidence documented before feature closure |

## Traceability Rules
Update `traceability.md` at each phase. Required links:
- `REQ → US`
- `US → TREQ`
- `US → E2E test cases`

Any missing link **blocks progression** to the next gate.

## Completion Criteria
- All linked REQ, US, and TREQ artifacts are Approved.
- Replacements/conflicts are approved and reflected in statuses.
- Tests are executed and evidence is documented.
- Traceability is complete and consistent.
