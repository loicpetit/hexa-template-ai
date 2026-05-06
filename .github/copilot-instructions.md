# Hexa Template — Global AI Agent Instructions

## Project Purpose
Deliver robust web application features with full traceability from business need to test evidence, using specialized AI agents and hexagonal architecture.

## Execution Model
- The workflow is **sequential and gated**.
- A next phase cannot start until the previous gate is **explicitly approved by the user**.
- All artifacts are markdown files stored in the repository.
- Before finalizing any REQ artifact, Need Collector must request a first-pass quality review from Product Owner (review-only, no US creation).
- Before finalizing any US artifact, Product Owner must request Gherkin testability feedback from Developer (review-only, no implementation).
- Before finalizing any TREQ artifact, Software Architect must request implementability feedback from Developer (review-only, no code writing).

## Folder Structure
```
requirements/                  ← REQ-XXXX files (Need Collector)
user-stories/                  ← US-XXXX files grouped by functionality (Product Owner)
docs/features/                 ← Feature user guides with examples (Product Owner)
technical-requirements/        ← TREQ-XXXX files (Software Architect)
docs/architecture/             ← Project architecture documentation maintained by Software Architect
docs/dev/                      ← Technical implementation notes and decisions (Developer)
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
- Requirement drafting workflow must include Product Owner first-pass feedback before REQ finalization.
- User story Gherkin scenarios must be reviewed by Developer for testability before US finalization.
- Technical requirements must be reviewed by Developer for implementability and clarity before TREQ finalization.
- Architecture documentation in `docs/architecture/` must stay aligned with approved technical requirements.
- Feature documentation in `docs/features/` must stay aligned with approved user stories and include real-world examples and how-to guides in plain business language.
- Developer documentation in `docs/dev/` must capture implementation decisions and technical rationale aligned with approved TREQs.
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
| **Gate 4** | All E2E tests **Pass** and feature guides documented before feature closure |

## Traceability Rules
Update `traceability.md` at each phase. Required links:
- `REQ → US` (Gate 2)
- `US → TREQ` (Gate 3)
- `US → E2E test cases` (Gate 4)
- `US → Feature Guide` (Gate 4, after implementation and testing)

Any missing link **blocks progression** to the next gate.

## Completion Criteria
- All linked REQ, US, and TREQ artifacts are Approved.
- Replacements/conflicts are approved and reflected in statuses.
- Tests are executed and evidence is documented.
- Feature guides are created after implementation and testing with real-world examples and how-to guides.
- Traceability is complete and consistent (REQ → US → TREQ → E2E → Feature Guide).
