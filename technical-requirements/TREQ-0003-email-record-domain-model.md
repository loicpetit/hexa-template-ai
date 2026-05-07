# TREQ-0003 - Email Record Domain Model

## Metadata
- ID: TREQ-0003
- Status: Approved
- Created: 2026-05-07
- Updated: 2026-05-07
- Author Agent: Software Architect
- Source User Stories: US-0001, US-0002, US-0003, US-0004, US-0005
- Related IDs: REQ-0001, REQ-0003, REQ-0004, TREQ-0001
- Source Links: [requirements/REQ-0001-email-crud-api.md](../requirements/REQ-0001-email-crud-api.md), [requirements/REQ-0003-email-record-response-fields-contract.md](../requirements/REQ-0003-email-record-response-fields-contract.md), [requirements/REQ-0004-email-audit-attribution.md](../requirements/REQ-0004-email-audit-attribution.md)
- Architecture Links: [docs/architecture/architecture-overview.md](../docs/architecture/architecture-overview.md)

## Technical Requirement Statement
The system shall define an Email Record domain entity that represents a business email address with associated metadata. The entity shall include: a unique identifier (id), email value (email address string), lifecycle timestamps (created, updated), and audit attribution fields (createdBy, updatedBy). The entity shall enforce validation rules for email format and required fields. The entity shall be framework-independent and testable in isolation from adapters and persistence mechanisms.

## Constraints
- Email value must be a valid email address format (conform to RFC 5322 basic pattern).
- Email value is mandatory (cannot be null or empty).
- Entity id must be unique across all email records.
- created timestamp must be set once at creation time and never changed.
- updated timestamp must be set at creation time and updated on any record modification.
- createdBy and updatedBy must be valid user identifiers (non-empty string).
- Entity must enforce all constraints in domain logic (not rely on database schema for validation).
- Entity must be immutable after creation (changes via update method only, which creates new version semantically).
- Entity must not depend on any framework, database library, or HTTP library.

## Existing Coverage Check
- Similar TREQ checked: None (first domain model TREQ)
- Already covered: REQ-0001, REQ-0003, REQ-0004 define what fields and audit is needed; technical domain entity design not specified
- Gap: Requirements specify fields and business rules; TREQ specifies how to implement them in domain layer

## Current Architecture Baseline
- Existing stack and patterns considered: Hexagonal architecture (TREQ-0001) defines domain layer responsibility
- Related approved TREQs: TREQ-0001 (domain layer), TREQ-0002 (audit attribution via user id)
- Consistency expectation for this decision: Entity must be testable in isolation; validation rules must be in entity, not in use case or adapter

## Technical Module Organization

### Domain Entity: EmailRecord
**Location**: `domain/entities/email-record`  
**Responsibility**: Encapsulate email business logic, enforce validation constraints, maintain invariants

**Entity Design Principles**:
- **Immutability by default**: Identity (id) and creation context (created, createdBy) immutable after creation; value and audit (updated, updatedBy) mutable via explicit update method
- **Factory methods for controlled construction**: EmailRecord.create() validates preconditions before entity instantiation
- **Validation colocated with data**: Email format validation, required field checks, audit field validation live in entity methods
- **Type-safe via strong typing**: All properties typed; compiler catches invalid assignments at build time
- **Exception-driven error handling**: Validation failures throw specific domain exceptions (InvalidEmailValueError, InvalidAuditError) for clear error semantics

**Entity Structure & Fields**:
- **id**: Unique identifier (UUID v4 recommended; generated at creation; immutable)
- **value**: Email address string (validated format per RFC 5322 basic pattern; required; mutable via update)
- **created**: Timestamp of entity creation (immutable after creation; required)
- **updated**: Timestamp of last modification (set at creation; updated on each modification; required)
- **createdBy**: User id who created record (immutable after creation; required; from authenticated user)
- **updatedBy**: User id who last modified record (set at creation; updated on each modification; required)

**Entity Responsibilities**:
- **Enforce email format validation**: Only accept valid email addresses (reject invalid formats, null, empty)
- **Enforce audit field requirements**: createdBy and updatedBy must be non-empty user identifiers
- **Prevent invalid state transitions**: created timestamp never changes; updatedBy reflects current actor
- **Support creation via factory**: EmailRecord.create(value, createdBy) → validates → instantiates
- **Support updates via method**: existing.update(newValue, updatedBy) → validates → returns new entity instance (semantic versioning)
- **Serialize to DTO**: Convert internal representation to API contract (ISO 8601 timestamps, field names)

---

## Alternatives

### Option A: Rich Domain Entity (Proposed)
**Approach**: Domain entity is a class with validation methods and business logic; strongly typed
- Entity enforces all constraints
- Factory methods (static create) control instantiation
- Validation in entity constructor/methods
- Update via method call (returns new version)
- Properties immutable where appropriate (id, created, createdBy)

**Pros**:
- Strong type safety: business logic enforced by compiler
- Encapsulation: validation and rules co-located with data
- Testability: entity can be tested in isolation (no frameworks)
- Self-documenting: code expresses business rules clearly

**Cons**:
- Slightly more code upfront (compared to anemic model)
- ORM mapping required (entity → table rows)

**Consistency impact**: Fully aligned with DDD and hexagonal architecture

---

### Option B: Anemic Data Model
**Approach**: Domain is just data (POJO/DTO); validation in use cases or services
- Entity is plain object: `{ id, value, created, ... }`
- No validation logic in entity
- Validation rules in separate service or use case

**Pros**:
- Simpler to understand initially
- Easy to serialize/deserialize
- Direct mapping to database

**Cons**:
- Business rules scattered across use cases
- Easy to create invalid state
- Difficult to maintain consistency
- Less testable (rules mixed with orchestration)

**Consistency impact**: Breaks DDD principles; not recommended for maintainable systems

---

### Option C: Event Sourcing (Alternative)
**Approach**: Entity is immutable; changes recorded as domain events
- Entity is reconstructed from event stream
- Create/Update operations generate events
- Replay events to reconstruct entity state

**Pros**:
- Complete audit trail (all changes recorded)
- Temporal queries (state at any point in time)
- Event-driven architecture foundation

**Cons**:
- Significant complexity for CRUD feature
- Requires event store infrastructure
- Overkill for current scope

**Consistency impact**: Too advanced for baseline; consider if audit requirements evolve

---

## Top 3 Ranking

1. **Option A: Rich Domain Entity (Proposed)**
   - Why: Best balance of clarity, maintainability, and business logic encapsulation. Aligns with DDD principles. Testable in isolation. Business rules explicit in code.

2. **Option B: Anemic Data Model**
   - Why: Simpler to code initially but leads to maintenance issues as complexity grows. Not recommended unless team has strong discipline.

3. Option C: Event Sourcing
   - Why: Excellent for audit-heavy systems but overkill for current CRUD scope. Consider for future if historical queries are required.

## Trade-Offs

- **Code volume vs. Maintainability**: Rich entity requires more code but business rules are explicit and isolated. Trade accepted: clarity over brevity.

- **ORM complexity vs. Domain safety**: Rich entity requires ORM mapping but prevents invalid state. Trade accepted: ORM handles mapping; domain keeps invariants.

- **Mutability vs. Safety**: Entity properties are immutable where appropriate (id, created); update returns new version. Trade accepted: slight overhead prevents state corruption.

## Impact Analysis

- **Functional impact**:
  - Email records stored with all required fields (id, value, created, updated, createdBy, updatedBy)
  - Email format validation enforced (prevents invalid emails)
  - Audit fields captured on every operation
  - Hard delete removes all record data (no soft delete)

- **Module impact**:
  - Adds: EmailRecord domain entity class
  - Adds: Email validation functions
  - Adds: Domain exceptions (InvalidEmailValueError, etc.)
  - Adds: EmailRecordDTO for API responses
  - Modifies: Use cases (call EmailRecord.create, entity.update)
  - Modifies: Driven adapters (map entity ↔ database rows)

- **Interface and contract impact**:
  - Internal: Use cases work with EmailRecord entity and return EmailRecordDTO
  - External: API returns EmailRecordDTO (REST JSON)
  - Port: Repository port works with EmailRecord domain entity (not DTO)

- **Backward compatibility**: N/A (new project)

- **Data and migration impact**: N/A (new project)

- **Operational impact**:
  - Storage: Database stores email records with all audit fields
  - Indexing: id (primary key), value (unique constraint recommended)
  - Query efficiency: Repository adapter optimizes queries for findById, findAll, delete

- **Security and compliance impact**:
  - Validation: Invalid email formats rejected at domain layer (prevent injection)
  - Audit: All operations traced with createdBy/updatedBy
  - Privacy: Audit fields contain user id only (not full user info)

- **Testing impact**:
  - Unit tests: EmailRecord entity validates format, prevents invalid state
  - Unit tests: Factory method (create) tests constraint enforcement
  - Unit tests: Update method tests timestamp and updatedBy changes
  - Integration tests: Verify entity persists to database with all fields
  - E2E tests: Verify API returns all fields in response

- **Traceability impact**:
  - REQ-0001, REQ-0003, REQ-0004 → TREQ-0003 (domain model implements requirements)
  - US-0001 through US-0004 → TREQ-0003 (each use case creates/updates/deletes EmailRecord)
  - E2E tests verify response includes id, value, created, createdBy, updated, updatedBy

## Recommendation
- **Proposed best option**: Option A — Rich Domain Entity
- **Why**: 
  - Encapsulates business rules (validation, audit attribution)
  - Highly testable (no framework dependencies)
  - Prevents invalid state (factory methods, type safety)
  - Clear and maintainable (business logic explicit in code)
  - Aligns with DDD and hexagonal architecture principles
- **Final decision owner**: Requester

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|-----------|
| Email validation too strict (reject valid emails) | Low | Low | Use permissive regex (RFC 5322); add future option for strict validation |
| Entity validation diverges from database schema | Low | Medium | Schema mirrors entity constraints; document both in TREQ |
| Immutable entity overhead | Low | Low | Modern JS engines optimize; profile if performance issue emerges |
| ORM mapping complexity | Medium | Low | Use established ORM (TypeORM, Sequelize); map entity ↔ DTO ↔ table row |

## Consistency Exception Assessment
- Exception needed: No
- Rich domain entity aligns with hexagonal architecture and DDD best practices

## Validation
- Requester validation required: No (Option A is recommended baseline)
- Validation status: Approved ✓ (2026-05-07)
- Requester selected option: Option A — Rich Domain Entity (POC phase, approved for implementation)

## Notes
- Email value validation uses permissive regex suitable for 99% of cases. Future enhancement: add DNS/SMTP validation if business requires.
- Timestamps stored as Date objects internally; serialized as ISO 8601 strings in DTO and API responses.
- Update method returns a new EmailRecord instance (semantic immutability); existing references don't change.
- Audit fields (createdBy, updatedBy) contain user id strings for traceability; user name/email can be resolved from auth service if needed in future.

## Architecture Best Practices Compliance

- **Separation of concerns**: ✓ Entity encapsulates email rules; validation logic colocated with data; audit fields managed by entity
- **Coupling/cohesion**: ✓ High cohesion (entity owns all email-related rules); low coupling (no external dependencies)
- **Clear module boundaries and contracts**: ✓ Entity interface clear; DTO contract defined for API; exceptions explicit
- **Testability and observability**: ✓ Entity testable without frameworks; validation errors clear and specific
- **Security-by-design**: ✓ Validation prevents injection; audit fields immutable after creation
- **Maintainability and evolvability**: ✓ Business rules explicit and co-located; easy to add new validations or fields in future
