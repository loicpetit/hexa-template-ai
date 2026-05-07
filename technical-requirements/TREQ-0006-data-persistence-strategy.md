# TREQ-0006 - Data Persistence Strategy

## Metadata
- ID: TREQ-0006
- Status: Draft
- Created: 2026-05-07
- Updated: 2026-05-07
- Author Agent: Software Architect
- Source User Stories: US-0001, US-0002, US-0003, US-0004
- Related IDs: REQ-0001, TREQ-0001, TREQ-0003
- Source Links: [requirements/REQ-0001-email-crud-api.md](../requirements/REQ-0001-email-crud-api.md)
- Architecture Links: [docs/architecture/architecture-overview.md](../docs/architecture/architecture-overview.md)

## Technical Requirement Statement
The system shall persist email records using a database backend accessed through a repository port interface. The repository port shall abstract database implementation details, allowing different database technologies and query strategies to be swapped without changing use cases or domain logic. The repository shall implement ACID-compliant persistence (transactions for data consistency) and shall provide query operations for create, read, read-all, update, and delete by id. Records shall include all required fields (id, value, created, updated, createdBy, updatedBy) with constraints enforced at both domain and database layers.

## Constraints
- Repository must implement ACID transactions (atomicity, consistency, isolation, durability).
- Create operation must ensure id uniqueness; duplicate id attempts rejected with error.
- Email value should be unique across records (optional per option below; confirmed at Gate 3).
- Read operations must return full EmailRecord entity or null if not found.
- Update operations must be atomic (either fully update or fully rollback).
- Delete operation must permanently remove record (no soft delete).
- All database errors must be mapped to domain exceptions (not leak database details to use cases).
- Repository implementation must be pluggable via IEmailRepository port interface.
- Database schema must enforce constraints where appropriate (id primary key, value unique, not-null).

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

**Interface**:
```typescript
interface IEmailRepository {
  /**
   * Save a new email record or update existing
   * @param email - EmailRecord entity to persist
   * @returns - Persisted entity
   * @throws - DuplicateKeyError if id already exists (on create)
   * @throws - RepositoryError on database errors
   */
  save(email: EmailRecord): Promise<EmailRecord>
  
  /**
   * Find email record by id
   * @param id - Email record id
   * @returns - EmailRecord if found, null otherwise
   * @throws - RepositoryError on database errors
   */
  findById(id: string): Promise<EmailRecord | null>
  
  /**
   * Find all email records
   * @returns - Array of EmailRecord (empty if none exist)
   * @throws - RepositoryError on database errors
   */
  findAll(): Promise<EmailRecord[]>
  
  /**
   * Delete email record by id
   * @param id - Email record id to delete
   * @returns - void
   * @throws - NotFoundError if record doesn't exist
   * @throws - RepositoryError on database errors
   */
  delete(id: string): Promise<void>
}
```

**Exception Mapping**:
```typescript
class DuplicateKeyError extends Error { }
class NotFoundError extends Error { }
class RepositoryError extends Error { }
```

---

### Repository Driven Adapter (Implementation)
**Location**: `adapters/driven/repository-adapter`  
**Responsibility**: Implement IEmailRepository using database technology

**Subcomponents**:
- **Schema Mapper**: Convert domain entity ↔ database row
- **Query Builder**: Generate SQL or native database queries
- **Transaction Handler**: Begin/commit/rollback transactions
- **Error Handler**: Map database errors to domain exceptions

---

## Database Schema

### Email Records Table
```sql
CREATE TABLE email_records (
  -- Identity
  id VARCHAR(36) PRIMARY KEY,                    -- UUID v4
  
  -- Business data
  value VARCHAR(254) NOT NULL UNIQUE,           -- Email address (RFC 5321 limit: 254)
  
  -- Lifecycle
  created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  
  -- Audit
  created_by VARCHAR(255) NOT NULL,              -- User id
  updated_by VARCHAR(255) NOT NULL,              -- User id
  
  -- Indexes
  INDEX idx_created_by (created_by),
  INDEX idx_updated_by (updated_by),
  INDEX idx_updated (updated)
)
```

**Constraints Rationale**:
- `id`: Primary key ensures uniqueness; UUID v4 allows distributed generation
- `value`: Unique constraint prevents duplicate email addresses; not-null ensures required field
- `created`, `updated`: Timestamps track lifecycle; not-null ensures valid data
- `created_by`, `updated_by`: Store user id for audit trail; not-null enforces actor attribution
- **Indexes**: Optimize queries for findAll (by creation order), audit queries (by actor)

---

## Alternatives

### Option A: Relational Database (PostgreSQL / MySQL)
**Approach**: Traditional SQL database with ACID guarantees
- Schema: email_records table with constraints
- ORM: TypeORM, Sequelize, or Prisma for entity mapping
- Transactions: SQL transactions for atomic operations

**Pros**:
- ACID compliance built-in (guaranteed consistency)
- Mature, battle-tested, widely used
- Strong schema enforcement (constraints at database layer)
- Good performance for structured data
- Easy backup and recovery
- Open-source option (PostgreSQL)

**Cons**:
- Schema migrations required for changes
- Requires database server setup and management
- Scaling for writes (vertical scaling; horizontal requires sharding)

**Consistency impact**: Industry standard for business data; aligns with transactional requirements

---

### Option B: NoSQL Document Database (MongoDB)
**Approach**: Flexible schema, document-oriented storage
- Schema: Collections of JSON documents (no predefined schema)
- Query: Native query language or aggregation pipeline
- Transactions: Multi-document transactions (MongoDB 4.0+)

**Pros**:
- Flexible schema (changes without migrations)
- Natural JSON serialization (no ORM mapping needed)
- Horizontal scalability (sharding)
- Good for semi-structured data

**Cons**:
- Weaker ACID guarantees (eventual consistency by default)
- Requires explicit transaction setup (not automatic)
- No schema enforcement (mistakes harder to catch)
- Higher memory usage per document

**Consistency impact**: Less aligned with transactional requirements; better for high-scale read-heavy systems

---

### Option C: SQLite (File-Based SQL)
**Approach**: Lightweight, file-based SQL database
- Schema: Local SQLite file (no server needed)
- ORM: TypeORM, Sequelize, or Prisma
- Transactions: SQLite transactions for atomic operations

**Pros**:
- No server setup needed (single file)
- Easy to deploy and test
- Full ACID support
- Great for development/prototyping

**Cons**:
- Poor concurrency (locks entire file on write)
- No built-in replication (not suitable for production at scale)
- Limited network access (file-based only)

**Consistency impact**: Excellent for development; suitable for production only if single-writer, low-concurrency workload

---

## Top 3 Ranking

1. **Option A: PostgreSQL (Relational Database)**
   - Why: Best balance of ACID compliance, maturity, and scalability. Battle-tested for business systems. Open-source. Strong schema enforcement. Excellent support for complex queries and constraints.

2. **Option B: MongoDB (NoSQL Document)**
   - Why: Better for highly scalable, eventually-consistent systems. Consider if horizontal scaling becomes critical requirement.

3. **Option C: SQLite (File-Based SQL)**
   - Why: Perfect for development and testing. Acceptable for production if single-writer, low-concurrency workload. Not recommended for multi-instance deployment.

## Trade-Offs

- **Schema Flexibility vs. Data Safety**: Relational enforces schema (safety); NoSQL flexible but risky. Trade: Relational chosen for data integrity priority.

- **Scalability vs. Simplicity**: Relational simpler initially; NoSQL scales horizontally. Trade: Relational appropriate for baseline; can scale vertically or shard later if needed.

- **ACID Guarantees vs. Performance**: Relational prioritizes consistency (slower); NoSQL optimizes performance (eventual consistency). Trade: Consistency critical for financial/business data.

- **Deployment Complexity vs. Development Speed**: SQLite fast to deploy; production PostgreSQL requires more setup. Trade: PostgreSQL for production-ready baseline.

## Impact Analysis

- **Functional impact**:
  - Email records persisted durably (survive server restarts)
  - ACID compliance ensures no partial updates
  - Constraints prevent invalid data at database layer
  - Hard delete removes records completely

- **Module impact**:
  - Adds: Repository port interface
  - Adds: Repository driven adapter (database implementation)
  - Adds: Database schema (email_records table, indexes, constraints)
  - Adds: Schema migration scripts (if PostgreSQL selected)

- **Interface and contract impact**:
  - Internal: Use cases depend on IEmailRepository port; don't know database technology
  - Database: email_records table schema with 6 columns, constraints, indexes
  - Exception: Database errors mapped to domain exceptions (DuplicateKeyError, NotFoundError, RepositoryError)

- **Backward compatibility**: N/A (new project)

- **Data and migration impact**: 
  - Initial: Create email_records table at deploy time
  - Future: Schema changes via migrations (e.g., add columns, indexes)
  - Backup: Regular backups recommended (per deployment strategy)

- **Operational impact**:
  - Database setup: Deploy PostgreSQL server (if Option A)
  - Maintenance: Monitor database performance, disk usage; apply security patches
  - Backup/recovery: Regular backups and recovery testing required
  - Monitoring: Track query performance, slow queries, connection pool health

- **Security and compliance impact**:
  - Encryption: Database connections use SSL/TLS (deployment concern)
  - Backup encryption: Backups encrypted at rest (deployment concern)
  - Audit: Audit trail in audit_events table (linked to TREQ-0005)
  - GDPR: Right to be forgotten (delete operation removes all user data per TREQ-0004)

- **Testing impact**:
  - Unit tests: Mock repository; no database needed
  - Integration tests: In-memory or test database (SQLite for CI; PostgreSQL for production-like testing)
  - E2E tests: Real database (staging environment or docker container)
  - Fixtures: Seed test data before each test

- **Traceability impact**:
  - REQ-0001 (CRUD operations) → TREQ-0006 (persistence strategy)
  - US-0001-0004 → TREQ-0006 (each use case uses repository)
  - E2E tests verify data persists across requests

## Recommendation
- **Proposed best option**: Option A — PostgreSQL (Relational Database)
- **Why**: 
  - ACID compliance essential for business data consistency
  - Mature, battle-tested, industry standard
  - Strong schema enforcement prevents invalid data
  - Excellent scaling story (vertical and eventual horizontal via sharding)
  - Open-source reduces cost
  - Wide support community and extensive documentation
  - Natural fit for transactional email record management
- **Final decision owner**: Requester

**Implementation notes if PostgreSQL selected**:
- ORM: Recommend TypeORM or Prisma (both have excellent Node.js support)
- Connection pooling: Use pg pool for efficient connection management
- Migrations: Version schema changes in repository; run on deploy
- Backup: Automated daily backups (deployment concern)
- Monitoring: Monitor slow queries; index optimization

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|-----------|
| Database becomes single point of failure | Medium | High | High availability setup (replication, failover); regular backups |
| Query N+1 problem (inefficient queries) | Medium | Low | Use ORM eager loading; profile queries; add indexes |
| Unique constraint violation (duplicate email) | Low | Medium | Add unique constraint in schema; validate before insert; retry logic |
| Data corruption on hard delete | Low | High | Backup before major operations; soft delete option for recovery (future) |
| Slow queries as data grows | Low | Medium | Monitor performance; add indexes; optimize queries; consider archiving old data |

## Consistency Exception Assessment
- Exception needed: No
- PostgreSQL aligns with transactional system requirements and industry best practices

## Validation
- Requester validation required: Yes
- Validation status: Pending
- Requester selected option: (Required before Approved) — Choose Option A (PostgreSQL), B (MongoDB), or C (SQLite)

## Notes
- Email value uniqueness (UNIQUE constraint) prevents duplicate email addresses. This is a strong business rule but can be relaxed if multiple records per email are needed (change at Gate 3).
- Repository pattern (port interface) allows swapping databases later. If PostgreSQL doesn't meet requirements, adapter can be replaced with MongoDB adapter without changing use cases.
- ORM or query builder (TypeORM, Sequelize, Prisma) should be selected per development team preference. All three work well with PostgreSQL.
- Connection pooling essential for production; configure pool size based on concurrency expectations.

## Architecture Best Practices Compliance

- **Separation of concerns**: ✓ Repository abstracted via port; use cases don't know database technology
- **Coupling/cohesion**: ✓ Low coupling (IEmailRepository interface); high cohesion (repository focused on persistence)
- **Clear module boundaries and contracts**: ✓ Port interface defines operations; schema mirrors entity structure
- **Testability and observability**: ✓ Mock repository for unit tests; query logging for performance debugging
- **Security-by-design**: ✓ ACID compliance; encrypted connections; audit trail; backup strategy
- **Maintainability and evolvability**: ✓ Schema versioning; migrations; ability to swap database technology via adapter
