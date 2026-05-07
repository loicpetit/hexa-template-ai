# TREQ-0006 - Data Persistence Strategy

## Metadata
- ID: TREQ-0006
- Status: Approved
- Created: 2026-05-07
- Updated: 2026-05-07
- Author Agent: Software Architect
- Source User Stories: US-0001, US-0002, US-0003, US-0004, US-0005
- Related IDs: REQ-0001, TREQ-0001, TREQ-0003
- Source Links: [requirements/REQ-0001-email-crud-api.md](../requirements/REQ-0001-email-crud-api.md)
- Architecture Links: [docs/architecture/architecture-overview.md](../docs/architecture/architecture-overview.md)

## Technical Requirement Statement
The system shall persist email records in a process-scoped in-memory store, implemented as a singleton module that holds data for the lifetime of the running process. The repository port interface (IEmailRepository) abstracts the in-memory implementation from use cases and domain logic, allowing a database-backed adapter to be swapped in later without changing any business logic. Data does not survive process restarts; this is accepted for the current scope.

## Constraints
- In-memory store is process-scoped: data does not survive server restarts. This is explicitly accepted for the current scope.
- The store must be a singleton module (single shared instance per process).
- Create operation must enforce id uniqueness; duplicate id attempts rejected with `DuplicateKeyError`.
- Email value must be unique across records; duplicate value attempts rejected.
- Read operations must return the full EmailRecord entity or null if not found.
- Delete operation must permanently remove the record; throws `NotFoundError` if absent.
- All store errors must be mapped to domain exceptions; no implementation details leaked to use cases.
- Repository implementation must be pluggable via IEmailRepository port interface so a database adapter can replace it without touching use cases or domain.

## Existing Coverage Check
- Similar TREQ checked: None (first persistence TREQ)
- Already covered: REQ-0001 specifies CRUD operations; technical persistence strategy not specified
- Gap: Requirements define what to persist; TREQ specifies how (database technology, schema, query strategy)

## Current Architecture Baseline
- Existing stack and patterns considered: Hexagonal architecture (TREQ-0001) defines repository as outbound port
- Related approved TREQs: TREQ-0001 (port interface), TREQ-0003 (EmailRecord entity schema)
- Consistency expectation for this decision: Repository adapter implements IEmailRepository port; domain entity drives schema design

## Technical Module Organization

### Repository Port (Contract)
**Location**: `ports/repository-port.ts`
**Responsibility**: Abstract data persistence from use cases

**Contract**:

| Operation | Signature | Behavior |
|-----------|-----------|----------|
| `save` | `save(email)` → persisted entity | Creates or updates; throws `DuplicateKeyError` if id already exists on create |
| `findById` | `findById(id)` → entity or null | Returns entity if found, null otherwise |
| `findAll` | `findAll()` → array of entities | Returns all records; empty array if none |
| `delete` | `delete(id)` → void | Removes record; throws `NotFoundError` if absent |

**Exception types**:
- `DuplicateKeyError`: raised when saving an entity whose id already exists
- `NotFoundError`: raised when deleting a non-existent record
- `RepositoryError`: raised on unexpected database-level failures

---

### Repository Driven Adapter (Implementation)
**Location**: `adapters/driven/repository-adapter`
**Responsibility**: Implement IEmailRepository using an in-memory singleton store

**Subcomponents**:
- **In-memory store**: Singleton module holding a `Map<id, EmailRecord>` (or equivalent keyed collection) as the primary data structure
- **Uniqueness guard**: On `save` (create path), verify `id` and `value` are not already present; throw `DuplicateKeyError` if either conflicts
- **Error handler**: Map store-level errors to domain exceptions (`DuplicateKeyError`, `NotFoundError`)

**Lifecycle**: Store is initialized at module load time and lives for the full process lifetime. No setup, teardown, or migration steps required.

---

## Data Structure

The in-memory adapter maintains the full EmailRecord entity in the store. No schema mapping or serialization is needed — domain entities are stored and retrieved directly.

**Fields held per record** (mirrors EmailRecord domain entity per TREQ-0003):

| Field | Notes |
|-------|-------|
| `id` | UUID v4; store key |
| `value` | Email address; must be unique across all records |
| `created` | UTC timestamp; set at creation, immutable |
| `updated` | UTC timestamp; updated on each modification |
| `createdBy` | User id; set at creation, immutable |
| `updatedBy` | User id; updated on each modification |

---

## Alternatives

### Option A: In-Memory Singleton Store (Selected)
**Approach**: Process-scoped singleton module holds all records in a keyed in-memory collection for the process lifetime.

**Pros**:
- Zero setup — no external dependency, no server, no schema
- Instant reads and writes (no I/O latency)
- Trivial to test (fresh instance per test)
- Fully consistent with hexagonal architecture (implements IEmailRepository port)
- Adapter swap to a real database requires no changes to use cases or domain

**Cons**:
- Data lost on process restart (not durable)
- Not suitable for multi-instance deployments (each process has its own store)
- No built-in query/filtering (must iterate in adapter)

**Consistency impact**: Aligned — implements the same port interface; all other modules are unaffected

---

### Option B: SQLite (File-Based SQL)
**Approach**: Lightweight file-backed SQL database; no server needed.

**Pros**: Durable (survives restarts); full SQL; ACID; no server
**Cons**: File locking limits concurrency; requires schema migration tooling; more setup than in-memory

**Consistency impact**: Aligned — same port interface; adapter complexity higher

---

### Option C: PostgreSQL (Relational Database)
**Approach**: Full relational database server with ACID guarantees.

**Pros**: Production-grade; scalable; strong constraints
**Cons**: Requires server setup and management; over-engineered for current scope

**Consistency impact**: Aligned — same port interface; highest operational overhead

---

## Top 3 Ranking

1. **Option A: In-Memory Singleton Store** — Zero friction, no dependencies, easily replaceable via port interface. Right choice for current scope.
2. **Option B: SQLite** — Natural upgrade path when durability is needed; same hexagonal alignment.
3. **Option C: PostgreSQL** — Right choice for production scale; out of scope for now.

If fewer than 3 realistic options exist, list only valid options and explain why.

## Trade-Offs

- **Durability vs. Simplicity**: In-memory store loses data on restart; accepted trade-off for zero-setup simplicity in current scope.
- **Multi-instance vs. Single-process**: In-memory store is not shareable across instances; acceptable for single-process deployment.
- **Evolvability**: Port interface ensures this decision is reversible — replacing the adapter with SQLite or PostgreSQL later requires no changes to use cases or domain.

## Impact Analysis

- **Functional impact**:
  - Email records available for the full process lifetime
  - Data is lost on process restart (explicitly accepted)
  - All CRUD operations work identically to a database-backed adapter from the use case perspective

- **Module impact**:
  - Adds: Repository port interface (`ports/repository-port.ts`)
  - Adds: In-memory repository adapter (`adapters/driven/repository-adapter`)
  - No database schema, migration scripts, or ORM dependency needed

- **Interface and contract impact**:
  - Internal: Use cases depend only on IEmailRepository port; unaware of in-memory implementation
  - Exception contract: `DuplicateKeyError`, `NotFoundError` raised as per port contract

- **Backward compatibility**: N/A (new project)

- **Data and migration impact**: None — no schema, no migration tooling required

- **Operational impact**:
  - No external dependency to deploy or maintain
  - Process restart clears all data (acceptable for current scope)
  - No connection pooling, backup, or monitoring infrastructure needed

- **Security and compliance impact**:
  - Data held in process memory; no at-rest encryption needed at this stage
  - Audit trail still captured via IAuditLogger (TREQ-0005) regardless of store

- **Testing impact**:
  - Unit tests: Use a fresh in-memory adapter instance per test (no mocking needed)
  - Integration tests: Same adapter; no test database setup
  - E2E tests: Data seeded programmatically before each test run

- **Traceability impact**:
  - REQ-0001 (CRUD operations) → TREQ-0006 (persistence strategy)
  - US-0001 through US-0005 → TREQ-0006 (each use case uses repository)

## Recommendation
- **Proposed best option**: Option A — In-Memory Singleton Store
- **Why**: Zero setup, no external dependencies, instant reads/writes, trivial to test. The hexagonal port interface fully preserves the option to swap to SQLite or PostgreSQL later without touching use cases or domain.
- **Final decision owner**: Requester

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|-----------|
| Data loss on process restart | Certain | Accepted | Explicitly accepted for current scope; swap adapter to SQLite/PostgreSQL when durability is needed |
| Unique constraint violation (duplicate email) | Low | Medium | Guard in adapter before insert; throw `DuplicateKeyError` |
| Memory growth with large datasets | Low | Low | Acceptable for current scope; monitor if data volume grows significantly |

## Consistency Exception Assessment
- Exception needed: No
- In-memory store is the simplest valid implementation of the IEmailRepository port. Durability trade-off is explicitly accepted for the current scope.

## Validation
- Requester validation required: Yes
- Validation status: Approved
- Requester selected option: A — In-Memory Singleton Store

## Notes
- Data loss on restart is explicitly accepted. When durability becomes a requirement, swap the in-memory adapter for a SQLite or PostgreSQL adapter — no changes needed in use cases or domain.
- Email value uniqueness is enforced in the adapter (guard before insert); ensures no duplicate addresses.
- The in-memory store requires no ORM, schema, or migration tooling.

## Architecture Best Practices Compliance

- **Separation of concerns**: ✓ Repository abstracted via port; use cases are unaware of in-memory implementation
- **Coupling/cohesion**: ✓ Low coupling (IEmailRepository interface); adapter is self-contained
- **Clear module boundaries and contracts**: ✓ Port interface defines all operations; adapter implements them fully
- **Testability and observability**: ✓ Fresh store per test; no test database setup required
- **Security-by-design**: ✓ Data scoped to process; audit trail still captured via IAuditLogger
- **Maintainability and evolvability**: ✓ Adapter is the single point of change when upgrading to a database; zero impact on other layers
