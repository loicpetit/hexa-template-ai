# TREQ-0008 - Data Access Layer: ORM & Query Builder

> **DEPRECATED** — Superseded by TREQ-0006 (in-memory storage selected). No database is used; no ORM or query builder is required. If a database is introduced in the future, a new TREQ shall be created at that point.

## Metadata
- ID: TREQ-0008
- Status: Deprecated
- Created: 2026-05-07
- Updated: 2026-05-07
- Deprecated reason: TREQ-0006 selected in-memory singleton store (no database). ORM/query builder layer is not applicable.
- Replaced by: N/A (no replacement needed for current scope)
- Author Agent: Software Architect
- Source User Stories: US-0001, US-0002, US-0003, US-0004, US-0005
- Related IDs: TREQ-0006, TREQ-0007
- Source Links: [technical-requirements/TREQ-0006-data-persistence-strategy.md](TREQ-0006-data-persistence-strategy.md)
- Architecture Links: [docs/architecture/architecture-overview.md](../docs/architecture/architecture-overview.md)

## Technical Requirement Statement
The system shall use an ORM (Object-Relational Mapping) or query builder to abstract database access logic and reduce boilerplate. The ORM/query builder shall map domain entities to database tables and provide type-safe query construction. The ORM must support the repository port interface and shall not leak database details into domain logic. ORM choice shall balance ease of use, performance, and maintainability.

## Constraints
- ORM must support TypeScript with strong type safety (full TypeScript support, not just JavaScript).
- ORM must provide transaction support for ACID compliance (TREQ-0006).
- ORM must allow query result mapping to domain entities (not just database rows).
- ORM must provide query hooks/interceptors to enforce domain constraints at persistence layer.
- ORM must be database-agnostic where possible (or at minimum support PostgreSQL and SQLite for switching).
- ORM must have active maintenance and community support (recent releases, responsive issues).
- Repository adapter must remain independent from domain logic (adapter pattern respected).

## Existing Coverage Check
- Similar TREQ checked: None (ORM selection not yet specified)
- Already covered: TREQ-0006 specifies persistence strategy (PostgreSQL); ORM technology not specified
- Gap: Database strategy defined but implementation tool not chosen

## Current Architecture Baseline
- Existing stack and patterns considered: TREQ-0007 (Node.js + TypeScript + Express.js); TREQ-0006 (PostgreSQL database)
- Related approved TREQs: TREQ-0006 (persistence), TREQ-0007 (technology stack)
- Consistency expectation for this decision: ORM must support TypeScript; must not violate hexagonal boundaries; must enable strong typing

## Alternatives

### Option A: TypeORM
**Profile**: Full-featured ORM with decorator-based entities
- **Approach**: Decorators on domain entities define database schema
- **Query**: QueryBuilder API for type-safe queries
- **Transactions**: Built-in transaction support
- **TypeScript**: Excellent; decorators are TypeORM's strength

**Pros**:
- Strong TypeScript support (decorators, type inference)
- Flexible schema generation and migrations
- Good documentation and large community
- Supports multiple databases (PostgreSQL, MySQL, SQLite)
- Query builder is readable and type-safe

**Cons**:
- Decorators mix infrastructure concerns with domain entities (slight violation of hexagonal purity; mitigated by DTO layer)
- Heavier than query builders (more features = more complexity)
- Migration management can be verbose

**Consistency impact**: Good for hexagonal (decorators can be isolated; mappers handle translation); moderate learning curve

---

### Option B: Prisma
**Profile**: Modern ORM with schema-first approach
- **Approach**: Define schema in prisma.schema; ORM generates types and client
- **Query**: Prisma Client API (chainable, type-safe)
- **Transactions**: Built-in transaction support
- **TypeScript**: Excellent; types auto-generated from schema

**Pros**:
- Schema-first approach (clear source of truth separate from code)
- Best-in-class TypeScript type generation (types auto-sync with schema)
- Cleanest API (most readable queries)
- Excellent documentation and growing community
- Built-in migrations (intuitive prisma migrate)
- Database-agnostic (swap PostgreSQL ↔ SQLite with schema change)

**Cons**:
- Newer project (less battle-tested than TypeORM)
- Opinionated (schema format, naming conventions)
- Smaller ecosystem (fewer plugins/extensions)

**Consistency impact**: Excellent for hexagonal (schema separate from domain; clean DTO mapping); modern tooling

---

### Option C: Sequelize + Raw Queries
**Profile**: Minimal ORM + raw SQL where needed
- **Approach**: Sequelize models for simple operations; raw SQL for complex queries
- **Query**: Sequelize methods + SQL strings
- **Transactions**: Sequelize transaction support
- **TypeScript**: Good support but less ergonomic than TypeORM/Prisma

**Pros**:
- Flexibility (raw SQL for complex queries)
- Lightweight (only what you need)
- Long-standing maturity (stable, predictable)
- Good documentation

**Cons**:
- Less type-safe for queries (raw SQL strings prone to errors)
- Boilerplate for simple operations
- TypeScript support feels bolted-on (not first-class)
- Smaller community than alternatives

**Consistency impact**: Adequate for hexagonal; less type safety means more runtime errors

---

## Top 3 Ranking

1. **Option B: Prisma**
   - Why: Best balance of modern tooling, type safety, and developer experience. Schema-first approach keeps persistence concerns separate from domain. Excellent documentation. Fast query construction.

2. **Option A: TypeORM**
   - Why: Battle-tested, large community, excellent TypeScript support. Decorator approach requires discipline to keep domain clean but works well with DTO layer.

3. **Option C: Sequelize + Raw SQL**
   - Why: Adequate but less ergonomic. Type safety not as strong. Consider only if Prisma/TypeORM unsuitable.

## Trade-Offs

- **Type Safety vs. Flexibility**: Prisma/TypeORM enforce types (fewer runtime errors); Sequelize more flexible but requires discipline. Trade: Type safety chosen for POC reliability.

- **Schema Management**: Prisma schema-first (single source of truth); TypeORM decorator-based (schema in code). Trade: Prisma cleaner for POC.

- **Community Size**: TypeORM larger; Prisma growing rapidly. Trade: Prisma's modern tooling outweighs smaller community for new project.

## Impact Analysis

- **Functional impact**: None — ORM is implementation detail; doesn't change API behavior

- **Module impact**:
  - Adds: ORM configuration and migrations
  - Modifies: Repository adapter (uses ORM instead of raw SQL)
  - Adds: Domain entity ↔ ORM model mapping layer (DTO)

- **Interface and contract impact**:
  - Internal: Repository port interface unchanged; implementation uses ORM
  - No external API impact

- **Backward compatibility**: N/A (new project); repository adapter can be reimplemented with different ORM

- **Data and migration impact**:
  - ORM migrations used to create/evolve schema
  - Seed scripts populated via ORM
  - Backup/recovery handled by database (not ORM concern)

- **Operational impact**:
  - Build: ORM type generation during build
  - Deployment: Migrations run before server startup
  - Performance: ORM query optimization (indexes, connection pooling)

- **Security and compliance impact**:
  - SQL injection prevention (ORM parameterizes queries)
  - Audit trail (ORM captures create/update timestamps)
  - Data privacy (ORM doesn't store sensitive data in memory)

- **Testing impact**:
  - Unit tests: Mock ORM calls; no database needed
  - Integration tests: In-memory SQLite or test database
  - Seed fixtures: ORM provides factories for test data

- **Traceability impact**:
  - TREQ-0006 (persistence strategy: PostgreSQL) → TREQ-0008 (ORM: Prisma)
  - US-0001-0004 → TREQ-0008 (all operations use repository with ORM)

## Recommendation
- **Proposed best option**: Option B — Prisma
- **Why**: 
  - Modern, first-class TypeScript support (types auto-generated)
  - Schema-first approach (clean separation: schema in prisma.schema, domain in domain/)
  - Most ergonomic API (minimal boilerplate for common operations)
  - Excellent documentation (fastest to productivity)
  - Built-in migrations (no external tool needed)
  - Good for POC (fast setup) and scales to production
- **Final decision owner**: Requester

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|-----------|
| Prisma project growth slows or becomes unmaintained | Low | Medium | Active maintenance as of 2026; thriving community; can migrate to TypeORM if needed (architecture supports swap) |
| ORM performance bottleneck (N+1 queries, slow joins) | Medium | Low | Enable query logging; profile queries; ORM provides optimization hints (batch loading, select fields) |
| Schema migrations conflict in team development | Medium | Low | Clear migration workflow; run migrations before each dev session; use test database for conflict resolution |
| Impedance mismatch (domain entity ≠ ORM entity) | Low | Low | Use DTO layer to map ORM models → domain entities; repository adapter handles translation |

## Consistency Exception Assessment
- Exception needed: No
- Prisma supports hexagonal architecture (schema separate from domain; adapter handles ORM details)

## Validation
- Requester validation required: Yes
- Validation status: Pending
- Requester selected option: (Required before Approved) — Choose Option A (TypeORM), B (Prisma), or C (Sequelize)

## Notes
- **Migration strategy**: Version migrations in repository (prisma/migrations/); run during deployment
- **Development workflow**: prisma studio for database GUI inspection (helpful during POC)
- **Testing**: Use SQLite in-memory for fast tests; PostgreSQL for integration tests
- **Performance**: Prisma provides query logging and performance insights; profile before optimizing

## Architecture Best Practices Compliance

- **Separation of concerns**: ✓ ORM kept in adapter layer; domain entities don't reference ORM
- **Coupling/cohesion**: ✓ Low coupling (repository port interface); high cohesion (ORM focused on persistence)
- **Clear module boundaries and contracts**: ✓ Schema separate from code; ORM client provides type-safe API
- **Testability and observability**: ✓ ORM provides query logging; mock ORM easy for unit tests
- **Security-by-design**: ✓ Parameterized queries (SQL injection prevention); transaction support (ACID)
- **Maintainability and evolvability**: ✓ ORM can be swapped if needed (architecture preserved); migrations version controlled
