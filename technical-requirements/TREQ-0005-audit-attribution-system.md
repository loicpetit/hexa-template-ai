# TREQ-0005 - Audit Attribution System

## Metadata
- ID: TREQ-0005
- Status: Approved
- Created: 2026-05-07
- Updated: 2026-05-07
- Author Agent: Software Architect
- Source User Stories: US-0001, US-0004
- Related IDs: REQ-0004, TREQ-0001, TREQ-0003
- Source Links: [requirements/REQ-0004-email-audit-attribution.md](../requirements/REQ-0004-email-audit-attribution.md)
- Architecture Links: [docs/architecture/architecture-overview.md](../docs/architecture/architecture-overview.md)

## Technical Requirement Statement
The system shall capture and persistently record the authenticated user identity for every email record create and update operation. The captured identity (actor id) shall be stored in the domain entity as createdBy (immutable after creation) and updatedBy (updated on each modification). The audit system shall provide a port interface (IAuditLogger) to abstract logging implementation details, allowing audit events to be logged to database, file, or external audit service without changing business logic.

## Constraints
- Audit capture must happen at application layer (use case orchestration), after authentication verification.
- createdBy must be captured at record creation time and remain immutable throughout record lifecycle.
- updatedBy must be captured on every record update and reflect the authenticated user who performed the update.
- Audit fields must not be null; if user authentication fails, operation must not proceed.
- Audit data must include: actor id, action (create/update), email record id, timestamp of operation.
- Audit logging must not block or fail the primary operation (audit is secondary concern).
- Audit logger implementation must be pluggable via IAuditLogger port interface.

## Existing Coverage Check
- Similar TREQ checked: None (first audit TREQ)
- Already covered: REQ-0004 specifies what audit (capture createdBy/updatedBy); technical how to implement and log is not specified
- Gap: Requirements define what fields are needed; TREQ specifies how to capture, store, and log audit events

## Current Architecture Baseline
- Existing stack and patterns considered: Hexagonal architecture (TREQ-0001) defines audit logger as outbound port
- Related approved TREQs: TREQ-0001 (port interface), TREQ-0002 (user authentication provides identity), TREQ-0003 (domain entity holds audit fields)
- Consistency expectation for this decision: Audit capture in use cases; logging via port interface; domain entity manages audit fields

## Technical Module Organization

### Audit Port (Contract)
**Location**: `ports/audit-logger-port.ts`
**Responsibility**: Abstract audit event logging from business logic

**Contract**:
- `log(action, actor, entity, timestamp)` → `void` (async, non-blocking)
  - `action`: one of `CREATE | UPDATE | DELETE`
  - `actor`: user id who performed the action
  - `entity`: the affected Email Record
  - `timestamp`: when the action occurred (UTC)

**Rationale**: Port interface allows swapping audit destinations (database, file, event stream) without changing use cases or domain.

---

### Audit Capture in Use Cases
**Location**: `application/use-cases/*`
**Responsibility**: Call audit logger after successful operation

#### Create Use Case Steps
1. Authenticate — resolve user via `IAuthProvider`; reject with `UnauthorizedError` if not authenticated
2. Execute business logic — create `EmailRecord` entity with `createdBy = user.id`
3. Persist — save entity via `IEmailRepository`
4. Audit log — call `IAuditLogger.log(CREATE, user.id, email, now)` non-blocking; audit failure must NOT fail the operation
5. Return created entity as DTO

#### Update Use Case Steps
1. Authenticate — resolve user; reject if not authenticated
2. Retrieve existing — load entity by id; reject with `NotFoundError` if absent
3. Execute business logic — apply update to entity with `updatedBy = user.id`
4. Persist — save updated entity
5. Audit log — call `IAuditLogger.log(UPDATE, user.id, updated, now)` non-blocking; audit failure must NOT fail the operation
6. Return updated entity as DTO

#### Delete Use Case Steps
1. Authenticate — resolve user; reject if not authenticated
2. Retrieve existing — load entity by id for audit capture; reject with `NotFoundError` if absent
3. Execute deletion — remove entity via repository
4. Audit log — call `IAuditLogger.log(DELETE, user.id, existing, now)` non-blocking; audit failure must NOT fail the operation
5. Return `void` (HTTP 204 No Content)

---

### Audit Logger Driven Adapter (Implementation)
**Location**: `adapters/driven/audit-logger-adapter`  
**Responsibility**: Implement IAuditLogger port; persist audit events to chosen destination

**Adapter Design**:
- **Event formatting**: Convert domain entity + action to audit event record (structured, timestamped)
- **Persistent storage**: Audit events stored via ORM (TREQ-0008), file system, or event stream (per technology selection)
- **Error isolation**: Adapter catches storage errors; logs them but does not propagate (preserves operation success)
- **Metadata enrichment**: Add optional context (request id, session id) for correlation

**Audit Event Data** (persisted for compliance):
- **Event id**: Unique audit event identifier
- **Timestamp**: When the audit event was logged (server time)
- **Action**: Type of business operation (CREATE, UPDATE, DELETE)
- **Actor**: User id who performed the action
- **Entity type**: Domain entity type affected (EmailRecord)
- **Entity id**: Unique identifier of affected entity
- **Entity snapshot** (optional): State of entity at time of operation (for forensic investigation)

**Adapter Integration with Logging Framework**:
- Audit events logged via structured logging framework (TREQ-0009)
- Events tagged as audit:true for filtering and analytics
- JSON structure enables querying via log aggregation services

---

## Audit Data Flow

```
Authenticated User Action (Create/Update/Delete)
  ↓
Use Case receives authenticated user from AuthProvider
  ↓
Domain entity created/updated with audit fields (createdBy/updatedBy)
  ↓
Repository persists entity
  ↓
Use case calls AuditLogger.log() with action, actor, entity
  ↓
Audit Logger Adapter formats and stores audit event
  ↓
Operation returns result; audit logged (or logged failure if adapter fails)
```

---

## Alternatives

### Option A: Audit in Application Layer (Proposed)
**Approach**: Use cases explicitly call audit logger after business operation succeeds
- Audit capture explicit in code (visible in use case)
- Non-blocking (audit failure doesn't fail operation)
- Audit logged only on successful operation

**Pros**:
- Clear and explicit (audit calls visible in use case code)
- Doesn't fail operation if audit fails (secondary concern)
- Flexible (can audit different fields per use case)
- Testable (mock audit logger for unit tests)

**Cons**:
- Requires discipline (developers must remember to log)
- Repeated code (similar audit calls in each use case)

**Consistency impact**: Aligned with hexagonal architecture; audit is cross-cutting but managed explicitly

---

### Option B: Aspect-Oriented Audit (Alternative)
**Approach**: Decorators/interceptors automatically log on method entry/exit
- @Auditable decorator on use case methods
- Framework intercepts and logs automatically
- No explicit audit calls in use case code

**Pros**:
- No repeated code (decorators handle it)
- Consistent audit behavior across all use cases
- Easy to enable/disable audit

**Cons**:
- Magic (implicit behavior harder to debug)
- Less flexible (hard to customize per operation)
- Requires framework/decorator support
- Harder to test (mock decorators)

**Consistency impact**: Less explicit but less code repetition

---

### Option C: Event Sourcing (Alternative)
**Approach**: All changes recorded as immutable events; audit is event stream
- Every create/update/delete generates event
- Entity reconstructed from event stream
- Audit trail is built-in (all events recorded)

**Pros**:
- Complete audit trail (every change recorded)
- Temporal queries possible (state at any point in time)
- Excellent for compliance/forensics

**Cons**:
- Significant complexity for CRUD scope
- Requires event store infrastructure
- Overkill unless audit is primary concern

**Consistency impact**: Too advanced for baseline; reconsider if compliance requirements increase

---

## Top 3 Ranking

1. **Option A: Audit in Application Layer (Proposed)**
   - Why: Clear and explicit. Audit calls visible in code. Non-blocking (secondary concern). Flexible per operation. Aligns with current architecture.

2. **Option B: Aspect-Oriented Audit**
   - Why: Reduces code repetition; consistent behavior. Consider if audit behavior becomes standard across many use cases.

3. Option C: Event Sourcing
   - Why: Excellent for high-audit-requirement systems but overkill for baseline CRUD. Reconsider if compliance requirements dominate future.

## Trade-Offs

- **Explicitness vs. Code Repetition**: Explicit calls (Option A) are clear but repeated; decorators (Option B) are implicit but less repeated. Trade accepted: explicitness aids maintenance.

- **Blocking vs. Availability**: Non-blocking audit (Option A) means operation succeeds even if audit fails. Trade accepted: better availability; audit is secondary concern.

- **Flexibility vs. Consistency**: Option A flexible (customize per use case); Option B consistent (same behavior everywhere). Trade accepted: flexibility appropriate for baseline.

## Impact Analysis

- **Functional impact**:
  - All create/update/delete operations now logged with actor identity
  - Audit trail captures who did what and when
  - Users can see createdBy/updatedBy in API responses
  - Deleted records still logged (for compliance)

- **Module impact**:
  - Adds: Audit logger port interface
  - Adds: Audit logger driven adapter (implementation)
  - Modifies: All use cases (add audit log calls)
  - Modifies: Domain entity (already has audit fields per TREQ-0003)

- **Interface and contract impact**:
  - Internal: Use cases call IAuditLogger port
  - Internal: Domain entity includes createdBy/updatedBy
  - External: API responses include createdBy/updatedBy fields
  - Data: Audit events persisted (new audit_events table or log file)

- **Backward compatibility**: N/A (new project)

- **Data and migration impact**: N/A (new project) — audit table created at deploy time

- **Operational impact**:
  - Storage: Audit events stored in database or file (growth depends on operation volume)
  - Retention: Audit events should be retained per compliance policy (e.g., 7 years)
  - Querying: Audit events indexed by actor, entityId, action, timestamp for reporting
  - Monitoring: Track audit log success rate; alert on audit failures

- **Security and compliance impact**:
  - Accountability: Every change attributed to authenticated user
  - Traceability: Audit trail enables forensic investigation
  - Compliance: Supports regulatory requirements (SOX, GDPR, HIPAA, etc.)
  - Non-repudiation: Users can't deny actions they performed (recorded in audit)

- **Testing impact**:
  - Unit tests: Mock audit logger; verify log calls with correct parameters
  - Integration tests: Verify audit events stored in database
  - E2E tests: Verify createdBy/updatedBy visible in API responses
  - Test fixture: Seed audit data for historical queries

- **Traceability impact**:
  - REQ-0004 (capture actor identity) → TREQ-0005 (audit system)
  - US-0001 (create) → TREQ-0005 (createdBy captured)
  - US-0003 (update) → TREQ-0005 (updatedBy captured)
  - E2E tests verify audit fields in responses and audit table populated

## Recommendation
- **Proposed best option**: Option A — Audit in Application Layer
- **Why**: 
  - Clear, explicit audit calls visible in use case code
  - Aligns with hexagonal architecture (audit as port interface)
  - Non-blocking (audit failure doesn't fail operation)
  - Flexible per operation (customize as needed)
  - Testable (mock audit logger)
  - Industry-standard approach for application audit
- **Final decision owner**: Requester

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|-----------|
| Audit logging performance impacts operation latency | Medium | Low | Make audit logging async/fire-and-forget; monitor latency |
| Audit log becomes too large; storage fills up | Low | High | Implement retention policy; archive old events; compress |
| Sensitive data logged in audit events | Low | High | Don't log passwords/tokens; sanitize audit event before logging |
| Audit logger fails; developers ignore error | Medium | Medium | Code review checklist; don't throw from audit logger (log failure only) |
| Audit fields incorrect (wrong actor) | Low | High | Unit test audit capture with mock auth provider; verify user id correct |

## Consistency Exception Assessment
- Exception needed: No
- Explicit audit in use cases aligns with hexagonal architecture and separation of concerns

## Validation
- Requester validation required: Yes
- Validation status: Approved
- Requester selected option: A — Audit in Application Layer
- Developer feedback required: Yes (implementability feedback requested)

## Notes
- Audit logging is non-blocking; if audit logger fails, operation still succeeds. Primary operation reliability is more important than audit logging reliability.
- Audit events should include entity snapshot (state at time of change) for forensic purposes.
- Actor id (user.id) is stored, not user name or email, for stability (name/email can change).
- Delete operations are audited (for compliance); deleted records can be recovered from audit log if needed.
- Future enhancement: Add retention policy and archive logic to manage audit table growth.

## Architecture Best Practices Compliance

- **Separation of concerns**: ✓ Audit capture isolated in port interface; use cases don't know implementation
- **Coupling/cohesion**: ✓ Low coupling (IAuditLogger interface); high cohesion (audit logger focused on event logging)
- **Clear module boundaries and contracts**: ✓ Port interface defines what audit adapter must provide; audit events schema defined
- **Testability and observability**: ✓ Mock audit logger easy for tests; audit events queryable for reporting
- **Security-by-design**: ✓ All operations attributed to authenticated user; audit trail enables accountability
- **Maintainability and evolvability**: ✓ Swapping audit destination (DB → event stream) requires only adapter change; use cases unchanged
