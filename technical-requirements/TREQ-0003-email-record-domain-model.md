# TREQ-0003 - Email Record Domain Model

## Metadata
- ID: TREQ-0003
- Status: Draft
- Created: 2026-05-07
- Updated: 2026-05-07
- Author Agent: Software Architect
- Source User Stories: US-0001, US-0002, US-0003, US-0004
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
**Location**: `domain/entities/email-record.ts`  
**Responsibility**: Encapsulate email business logic, validate constraints, maintain invariants

**Entity Structure**:
```typescript
class EmailRecord {
  // Identity
  readonly id: string                        // Unique identifier (UUID v4 recommended)
  
  // Business data
  value: string                              // Email address (validated format)
  
  // Lifecycle
  readonly created: Date                     // Immutable creation timestamp
  updated: Date                              // Updated timestamp (changes on every modification)
  
  // Audit
  readonly createdBy: string                 // User id who created this record
  updatedBy: string                          // User id who last updated this record
  
  // Private constructor (factory methods only)
  private constructor(
    id: string,
    value: string,
    created: Date,
    updated: Date,
    createdBy: string,
    updatedBy: string
  ) { ... }
  
  // Factory method: Create new email record
  static create(value: string, createdBy: string): EmailRecord {
    // Validate email value
    if (!value || !isValidEmail(value)) {
      throw new InvalidEmailValueError(`Email must be valid: ${value}`)
    }
    if (!createdBy) {
      throw new InvalidAuditError('createdBy must be a non-empty user id')
    }
    
    const now = new Date()
    return new EmailRecord(
      generateId(),                   // UUID v4
      value,
      now,
      now,
      createdBy,
      createdBy                       // Initially, updatedBy = createdBy
    )
  }
  
  // Update email value
  update(newValue: string, updatedBy: string): EmailRecord {
    if (!newValue || !isValidEmail(newValue)) {
      throw new InvalidEmailValueError(`Email must be valid: ${newValue}`)
    }
    if (!updatedBy) {
      throw new InvalidAuditError('updatedBy must be a non-empty user id')
    }
    
    // Create new instance with updated value and timestamp
    return new EmailRecord(
      this.id,                       // id unchanged
      newValue,                      // new value
      this.created,                  // created unchanged
      new Date(),                    // updated = now
      this.createdBy,                // createdBy unchanged
      updatedBy                      // updatedBy = actor
    )
  }
  
  // Convert to data transfer object (for API response)
  toDTO(): EmailRecordDTO {
    return {
      id: this.id,
      value: this.value,
      created: this.created.toISOString(),
      createdBy: this.createdBy,
      updated: this.updated.toISOString(),
      updatedBy: this.updatedBy
    }
  }
}
```

### Domain Validation Functions
**Location**: `domain/validators/email-validator.ts`  
**Responsibility**: Validate email format according to business rules

**Implementation**:
```typescript
/**
 * Validate email address format
 * Uses basic RFC 5322 pattern (suitable for most business use cases)
 * More complex validation (DNS lookup, SMTP check) can be added as future use case
 */
function isValidEmail(value: string): boolean {
  if (typeof value !== 'string') return false
  if (value.length === 0 || value.length > 254) return false
  
  // Basic email regex (RFC 5322 simplified)
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(value)
}
```

### Domain Exceptions (Bounded Context)
**Location**: `domain/exceptions/`  
**Responsibility**: Represent business rule violations

**Exceptions**:
- `InvalidEmailValueError`: Email value fails validation (format, length, null)
- `InvalidAuditError`: Audit field (createdBy, updatedBy) is invalid or missing
- `EmailRecordNotFoundError`: Requested email record does not exist

---

## Data Transfer Object (DTO)
**Location**: `application/dtos/email-record.dto.ts`  
**Responsibility**: Contract for API responses and internal use case communication

**DTO Structure**:
```typescript
interface EmailRecordDTO {
  id: string
  value: string
  created: string               // ISO 8601 timestamp
  createdBy: string            // User id (not user name for privacy)
  updated: string              // ISO 8601 timestamp
  updatedBy: string            // User id (not user name for privacy)
}
```

**Rationale**: DTO separates internal domain representation from external API contract. Timestamps serialized as ISO 8601 strings per REST conventions.

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
- Validation status: Proposed
- Developer feedback required: Yes (implementability and clarity feedback requested)

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
