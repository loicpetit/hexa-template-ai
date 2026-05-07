# TREQ-0001 - Hexagonal Architecture Module Organization

## Metadata
- ID: TREQ-0001
- Status: Approved
- Created: 2026-05-07
- Updated: 2026-05-07
- Author Agent: Software Architect
- Source User Stories: US-0001, US-0002, US-0003, US-0004
- Related IDs: REQ-0001, US-0001, US-0002, US-0003, US-0004
- Source Links: [docs/architecture/architecture-overview.md](../docs/architecture/architecture-overview.md)
- Architecture Links: [docs/architecture/architecture-overview.md](../docs/architecture/architecture-overview.md)

## Technical Requirement Statement
The system shall be organized using hexagonal architecture (ports and adapters pattern) with clear separation between business logic (domain layer), use case orchestration (application layer), external integrations (adapter layer), and abstraction contracts (ports layer). Each layer shall have explicit, testable responsibilities and modules shall communicate through well-defined port interfaces only.

## Constraints
- Domain logic must not depend on any framework, database, or HTTP library.
- All external integrations must be accessed through outbound ports only.
- Use cases (application layer) shall orchestrate domain logic but not implement business rules.
- Primary adapter (REST API) shall handle HTTP concerns only; no business logic in adapters.
- Module dependencies must always flow inward toward the domain (dependency inversion principle).

## Existing Coverage Check
- Similar TREQ checked: None (first TREQ in project)
- Already covered: No
- Gap: This is foundational architecture; all other TREQs inherit from this structure.

## Current Architecture Baseline
- Existing stack and patterns considered: N/A (new project)
- Related approved TREQs: None
- Consistency expectation for this decision: All future TREQs must align with hexagonal architecture layers and module organization defined here.

## Technical Module Organization

### Layer 1: Primary Adapter (Inbound Integration)
**Responsibility**: HTTP request handling, routing, request/response serialization  
**Module**: `adapters/primary/rest-api`
- Endpoint handlers: POST /emails, GET /emails/:id, GET /emails, PUT /emails/:id, DELETE /emails/:id
- Request validation and deserialization
- Response serialization
- HTTP status code mapping
- Error response formatting
- User context extraction from request (token/header parsing delegated to authentication adapter)

**Testability**: HTTP mocking frameworks; request/response contracts tested via integration tests

---

### Layer 2: Application Layer (Use Cases / Orchestration)
**Responsibility**: Business workflow coordination, cross-cutting concerns (auth, audit), use case logic  
**Modules**:
- `application/use-cases/create-email-record`
  - Coordinate creation: validate input → call domain → call repository → log audit → return result
  - Enforce authentication check (unauthenticated users rejected)
  - Capture createdBy from authenticated user context
- `application/use-cases/read-email-record`
  - Coordinate retrieval by id: check auth → call repository → return result
- `application/use-cases/list-email-records`
  - Coordinate list retrieval: check auth → call repository → return results
- `application/use-cases/update-email-record`
  - Coordinate update: validate input → check auth → call repository → call domain → log audit → return result
  - Capture updatedBy from authenticated user context
- `application/use-cases/hard-delete-email-record`
  - Coordinate deletion: check auth → call repository → return confirmation

**Testability**: Each use case is independently testable with mock repositories and auth providers

---

### Layer 3: Domain Layer (Business Logic)
**Responsibility**: Core business rules, entity constraints, invariants  
**Modules**:
- `domain/entities/email-record`
  - Email value validation (format, length, required field)
  - Entity identity (generated id)
  - Lifecycle fields: created (timestamp), updated (timestamp)
  - Audit fields: createdBy (actor id), updatedBy (actor id)
  - State consistency rules (e.g., created timestamp must not be changed after creation)
- `domain/value-objects/email-value` (optional refinement)
  - Encapsulate email string validation
  - Reusable across domain logic

**Testability**: Pure, framework-free business logic; unit tests exercise constraints directly

---

### Layer 4: Ports (Abstraction Contracts)
**Responsibility**: Define interfaces that isolate domain/application from infrastructure  
**Outbound Ports**:
- `ports/repository-port.ts`
  ```typescript
  interface IEmailRepository {
    save(email: EmailRecord): Promise<EmailRecord>
    findById(id: string): Promise<EmailRecord | null>
    findAll(): Promise<EmailRecord[]>
    delete(id: string): Promise<void>
  }
  ```
  **Rationale**: Repository pattern allows swapping databases without changing domain or use cases

- `ports/authentication-port.ts`
  ```typescript
  interface IAuthProvider {
    getCurrentUser(request: HttpRequest): Promise<User | null>
    // Returns { id: string, name: string, email: string }
  }
  ```
  **Rationale**: Abstracts authentication scheme (JWT, OAuth, etc.); testable with mock provider

- `ports/audit-logger-port.ts`
  ```typescript
  interface IAuditLogger {
    log(action: string, actor: User, entity: EmailRecord): Promise<void>
  }
  ```
  **Rationale**: Audit trail storage decoupled from domain; can log to database, file, or external service

---

### Layer 5: Driven Adapters (Outbound Integration)
**Responsibility**: Implement port interfaces; handle framework/database details  
**Modules**:
- `adapters/driven/repository-adapter`
  - Implement IEmailRepository
  - Database queries, ORM/query builder usage
  - Schema mapping (entity ↔ database rows)
  - Transaction handling
  - Error handling (duplicate key → retry logic, not found → null return)

- `adapters/driven/authentication-adapter`
  - Implement IAuthProvider
  - Token validation (JWT verification, OAuth callback, etc.)
  - User lookup from identity provider
  - Error handling (invalid token → null, timeout → exception)

- `adapters/driven/audit-logger-adapter`
  - Implement IAuditLogger
  - Format audit events
  - Persist to storage (database, logs, event stream)
  - Handle logging failures gracefully

---

## Module Communication & Data Flow

```
HTTP Request
     ↓
[Primary Adapter: REST API] ← Deserialize request, extract user context
     ↓
[Application Layer: Use Case] ← Authenticate user (via AuthPort), coordinate workflow
     ↓
[Domain Layer: Entity] ← Validate, enforce rules, generate response DTO
     ↓
[Outbound Port: Repository] ← Abstract persistence
     ↓
[Driven Adapter: Database] ← Execute SQL, return domain entity
     ↓
[Application Layer] ← Assemble response
     ↓
[Primary Adapter: REST API] ← Serialize response DTO
     ↓
HTTP Response
```

## Dependency Directions (Dependency Inversion)

```
Adapters depend on Ports
Ports depend on Domain
Use Cases depend on Domain and Ports (not on Adapters)
Domain depends on nothing (framework-free)

✓ Allowed: UseCase → Domain, UseCase → Port, Adapter → Port, Port → Domain
✗ Forbidden: Domain → Adapter, Domain → Port implementation, Adapter → UseCase
```

## Cross-Cutting Concerns Alignment

### Authentication & Authorization
- **Where checked**: Each use case receives authenticated user; checks before proceeding
- **How abstracted**: AuthPort interface; implementation can use JWT, OAuth, or mock
- **Testability**: Mock auth provider injects test user into use case

### Audit Attribution
- **Where captured**: Use cases capture user identity from auth context on create/update
- **How abstracted**: Domain entity holds createdBy/updatedBy; AuditLogger port logs events
- **Testability**: Mock audit logger captures log calls for verification

### Data Persistence
- **Where abstracted**: Repository port; application and domain don't know database details
- **How implemented**: Driven adapter implements port; uses ORM/query builder
- **Testability**: Mock repository returns test data; unit tests don't hit database

### Error Handling
- **Where handled**: Adapters catch framework errors and map to domain exceptions
- **How abstracted**: Use cases throw domain exceptions; primary adapter maps to HTTP status codes
- **Testability**: Unit tests exercise domain exception paths; integration tests verify HTTP responses

---

## Alternatives

### Option A: Layered Monolith (Alternative 1)
- All modules in single layer; direct dependencies between business logic and frameworks
- **Pros**: Simple initial setup; no interface abstraction overhead
- **Cons**: Tightly coupled to frameworks; hard to test domain logic in isolation; difficult to replace database/auth
- **Consistency impact**: Major break from hexagonal principles; limits evolvability

### Option B: Hexagonal Architecture (Proposed)
- Clear separation: domain, application, ports, adapters
- **Pros**: Business logic isolated from infrastructure; highly testable; easy to swap implementations; aligns with DDD principles
- **Cons**: More files/folders; requires discipline to maintain layer boundaries
- **Consistency impact**: Fully aligned; industry-standard pattern for maintainable systems

### Option C: Microservices (Alternative 2)
- Each use case as separate service; distributed architecture
- **Pros**: Independent scaling; technology heterogeneity; true isolation
- **Cons**: Overkill for CRUD; adds operational complexity; communication latency; data consistency challenges
- **Consistency impact**: Prematurely complex; not justified by current scope

## Top 3 Ranking
1. **Option B: Hexagonal Architecture**
   - Why: Proven pattern for testable, maintainable domain logic. Aligns with SOLID principles. Allows easy swapping of infrastructure without touching business logic. Perfect fit for future evolution.

2. **Option A: Layered Monolith**
   - Why: Simpler to understand for small teams; faster initial setup. Works fine for simple CRUD but becomes rigid as requirements grow.

3. Option C: Microservices
   - Why: Not recommended for current scope; adds unnecessary operational complexity.

## Trade-Offs

- **Flexibility vs. Complexity**: Hexagonal pattern requires more upfront structure but pays dividends in maintainability and testing. Trade accepted: long-term gain outweighs initial complexity.

- **Layer Boundaries vs. Code Organization**: Strict layer separation means more files/folders but clearer mental model. Trade accepted: clarity over brevity.

- **Testability vs. Speed**: Port interfaces require mock implementations but enable fast, isolated unit tests. Trade accepted: slower initial coding, faster overall iteration.

## Impact Analysis

- **Functional impact**: None — architecture doesn't change behavior; it organizes how behavior is implemented.

- **Module impact**:
  - Adds: 5 layers (Primary Adapter, Application, Domain, Ports, Driven Adapters)
  - Modules per layer: 1 primary adapter, 5 use cases, 2 domain entities, 3 ports, 3 driven adapters
  - Total: ~14 modules organized into clear folders

- **Interface and contract impact**:
  - Primary API: REST endpoints for CRUD operations (defined in TREQ-0004)
  - Internal contracts: Port interfaces for Repository, Auth, Audit (defined in port modules)
  - No external API impact; all contracts internal

- **Backward compatibility**: N/A (new project)

- **Data and migration impact**: N/A (new project)

- **Operational impact**:
  - Deployment: Single monolith; no infrastructure complexity
  - Observability: Each layer has clear responsibility; logging can be correlated by layer
  - Monitoring: Monitor application use cases, repository operations, and external service calls separately

- **Security and compliance impact**:
  - Authentication enforcement at application layer (all operations require authenticated user)
  - Audit trail at application layer (createdBy/updatedBy tracked for compliance)
  - No unencrypted sensitive data in logs (audit logger sanitizes if needed)

- **Testing impact**:
  - Unit tests: Domain entities and use cases (mock ports)
  - Integration tests: Full flow through adapters (real or in-memory database)
  - E2E tests: HTTP API (real database, real auth provider or mock)
  - Scope: All layers testable in isolation and integrated

- **Traceability impact**:
  - REQ → US: Functional requirements → use cases → use case modules
  - US → TREQ: Each use case traced to this architecture TREQ
  - TREQ → E2E: Architecture compliance verified by E2E tests exercising all layers

## Recommendation
- **Proposed best option**: Option B — Hexagonal Architecture
- **Why**: Optimal balance of clarity, testability, maintainability, and future extensibility. Industry-standard pattern that scales with project complexity. Aligns with development best practices (SOLID, DDD, separation of concerns).
- **Final decision owner**: Requester

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|-----------|
| Team unfamiliar with hexagonal pattern | Medium | Medium | Comprehensive architecture documentation, code examples in driven adapters, pair programming on first implementation |
| Developers violate layer boundaries | Medium | High | Code review checklist, static analysis (import restrictions), clear folder structure |
| Over-engineering for CRUD simplicity | Low | Medium | Regular architecture reviews; refactor to simpler pattern if scope stays trivial |
| Test fixture maintenance burden | Low | Low | Shared mock factories; use test databases for integration tests |

## Consistency Exception Assessment
- Exception needed: No
- This is the foundational architecture. All future TREQs must align with this structure.

## Validation
- Requester validation required: Yes
- Validation status: Approved ✓ (2026-05-07)
- Requester decision: Hexagonal Architecture approved as foundational structure

## Notes
- This TREQ establishes the structural foundation for all email record management features.
- All other TREQs (authentication, domain model, API endpoints, persistence) are designed to fit within this hexagonal architecture.
- Layer violations should be caught during code review and testing; architecture tests (ArchUnit or similar) are optional but recommended for enforcement.

## Architecture Best Practices Compliance

- **Separation of concerns**: ✓ Each layer has distinct responsibility (HTTP, orchestration, business logic, infrastructure)
- **Coupling/cohesion**: ✓ Low coupling (communication through ports), high cohesion (each module owns one concern)
- **Clear module boundaries and contracts**: ✓ Port interfaces explicitly define contracts; dependency direction enforced
- **Testability and observability**: ✓ Framework-free domain logic; each layer independently testable; clear responsibility per layer aids debugging
- **Security-by-design**: ✓ Authentication centralized at application layer; audit at application layer; no secrets in domain
- **Maintainability and evolvability**: ✓ Adding new use cases requires only new use case module + endpoint; swapping auth/database requires only adapter change
