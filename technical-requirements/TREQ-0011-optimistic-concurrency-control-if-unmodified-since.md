# TREQ-0011 - Optimistic Concurrency Control via If-Unmodified-Since

## Metadata
- ID: TREQ-0011
- Status: Approved
- Created: 2026-05-07
- Updated: 2026-05-07
- Author Agent: Software Architect
- Source User Stories: US-0004
- Related IDs: TREQ-0004, TREQ-0010
- Source Links: [technical-requirements/TREQ-0004-email-record-crud-api-endpoints.md](TREQ-0004-email-record-crud-api-endpoints.md), [technical-requirements/TREQ-0010-http-error-response-schema.md](TREQ-0010-http-error-response-schema.md)
- Architecture Links: [docs/architecture/architecture-overview.md](../docs/architecture/architecture-overview.md)

## Technical Requirement Statement
The system shall support optimistic concurrency control for email record updates using the HTTP `If-Unmodified-Since` header. Clients must include the `If-Unmodified-Since` header in PUT requests, specifying the email record's last known `updated` timestamp. The server shall verify that the record has not been modified since the specified timestamp. If the record was modified, the server shall reject the update with HTTP 409 Conflict and return the current state of the record. This prevents lost update conflicts when multiple users edit the same email record simultaneously.

## Constraints
- Update operations (PUT /emails/{id}) must require `If-Unmodified-Since` header (missing header → HTTP 400 Bad Request).
- Header value must be a valid RFC 7231 HTTP-date (same format as `Last-Modified` and `Date` headers).
- Server compares `If-Unmodified-Since` with the email record's current `updated` timestamp.
- If record's `updated` > `If-Unmodified-Since`, update rejected with HTTP 409 Conflict.
- HTTP 409 response must include the current email record state (so client can reconcile changes).
- Timestamps must be precise to the second (no milliseconds in HTTP headers; store with milliseconds internally).
- Optimistic concurrency applies only to update operations; create and delete are not affected.

## Existing Coverage Check
- Similar TREQ checked: None (concurrency control not specified in TREQ-0004)
- Already covered: TREQ-0004 specifies update endpoint; concurrency strategy not defined
- Gap: Update contract defined but conflict handling not specified

## Current Architecture Baseline
- Existing stack and patterns considered: REST API (TREQ-0004), email entity includes `updated` timestamp (TREQ-0003)
- Related approved TREQs: TREQ-0003 (entity has updated timestamp), TREQ-0004 (REST endpoints), TREQ-0010 (error response schema)
- Consistency expectation for this decision: All updates protected against concurrent modifications

## Concurrency Control Mechanism

### HTTP Header Contract

**Request** (PUT /emails/{id}):
```
PUT /emails/550e8400-e29b-41d4-a716-446655440000 HTTP/1.1
Content-Type: application/json
If-Unmodified-Since: Wed, 07 May 2026 14:30:00 GMT

{
  "value": "newemail@example.com"
}
```

**Response** (HTTP 200 OK — update succeeded):
```
HTTP/1.1 200 OK
Content-Type: application/json
Last-Modified: Wed, 07 May 2026 14:35:00 GMT

{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "value": "newemail@example.com"
}
```

**Response** (HTTP 409 Conflict — record was modified):
```
HTTP/1.1 409 Conflict
Content-Type: application/json
Last-Modified: Wed, 07 May 2026 14:32:30 GMT

{
  "error": {
    "code": "CONFLICT",
    "message": "Email record was modified since last read (updated 2026-05-07T14:32:30Z); update rejected",
    "timestamp": "2026-05-07T14:35:00Z"
  }
}
```

---

### Timestamp Handling

**HTTP Header Format** (per RFC 7231):
- Example: `Wed, 07 May 2026 14:30:00 GMT` (RFC 1123 date format)
- Precision: Seconds (HTTP headers have second resolution)
- Timezone: Always UTC (GMT)

**Internal Storage** (per RFC 3339):
- Example: `2026-05-07T14:30:00Z` or `2026-05-07T14:30:00.123Z` (millisecond precision)
- Precision: Milliseconds (for fine-grained audit trails)
- Timezone: Always UTC (Z)

**Comparison Logic**:
1. Client includes `If-Unmodified-Since: <RFC 7231 date>` in PUT request
2. Server converts RFC 7231 date to UTC millisecond timestamp (e.g., 1430989800000)
3. Server retrieves current email record
4. Server compares: current.updated (milliseconds) vs. If-Unmodified-Since (milliseconds, rounded to seconds)
5. If current.updated > If-Unmodified-Since: reject with HTTP 409
6. If current.updated ≤ If-Unmodified-Since: proceed with update

---

## Alternatives

### Option A: Optimistic Concurrency via If-Unmodified-Since (Proposed)
**How it works**:
- Client reads record; server returns `Last-Modified` header with `updated` timestamp
- Client includes `If-Unmodified-Since` header matching the timestamp in PUT request
- Server verifies record not modified since timestamp; rejects if modified

**Pros**:
- Industry standard (HTTP spec, supported by all clients/proxies)
- No server-side state (stateless)
- No locking (concurrent requests allowed)
- Clients can implement reconciliation (retry with latest state)
- Works with REST conventions

**Cons**:
- Requires client discipline (must include header)
- Handling timestamp precision tricky (seconds in headers vs. milliseconds in storage)
- Conflict resolution requires client logic (show diff, let user choose)

**Consistency impact**: Aligned with HTTP standards and REST best practices

---

### Option B: Pessimistic Locking (Alternative)
**How it works**:
- Client locks record before read; exclusive write lock held until release
- Only one client can modify record at a time
- Concurrent requests wait or fail immediately

**Pros**:
- Guaranteed no conflicts (lock prevents concurrent modifications)
- Simple conflict resolution (no retry needed)
- Familiar from relational databases

**Cons**:
- Requires server-side state (lock manager)
- Poor concurrency (serialized access; slow for high-concurrency workloads)
- Deadlock risk (clients must release locks)
- Not REST-idiomatic (locks are stateful)

**Consistency impact**: Not aligned with REST statelessness; not recommended

---

### Option C: Version Numbers (Alternative)
**How it works**:
- Each entity has version number (incremented on update)
- Client includes version in PUT request
- Server rejects if version doesn't match current version

**Pros**:
- Simpler than timestamps (no precision issues)
- Easy to implement (integer comparison)
- Natural conflict detection

**Cons**:
- Not standard HTTP (custom header)
- Timestamp also needed for audit (so adds extra field)
- Less familiar to REST developers

**Consistency impact**: Works but less standard than If-Unmodified-Since

---

## Top 3 Ranking

1. **Option A: Optimistic Concurrency via If-Unmodified-Since (Proposed)**
   - Why: HTTP standard, stateless, aligns with REST conventions. Timestamp already present in entity (TREQ-0003). Clear, well-understood mechanism.

2. **Option B: Pessimistic Locking**
   - Why: Guaranteed no conflicts but overkill for email records (low contention). Not REST-idiomatic.

3. **Option C: Version Numbers**
   - Why: Simpler than timestamps but less standard. If-Unmodified-Since already HTTP idiom.

## Trade-Offs

- **Complexity vs. Safety**: If-Unmodified-Since slightly complex (timestamp conversion); pessimistic locking simpler but overkill. Trade: If-Unmodified-Since chosen for REST alignment.

- **Server State vs. Statelessness**: Optimistic stateless; pessimistic requires state. Trade: Statelessness chosen for scalability.

- **Concurrency vs. Simplicity**: Optimistic allows high concurrency; pessimistic serializes. Trade: Optimistic chosen for POC.

## Impact Analysis

- **Functional impact**:
  - Update operations now protected against lost updates
  - Concurrent modifications visible (conflict response includes current state)
  - Clients can implement smart reconciliation

- **Module impact**:
  - Adds: Concurrency check logic in update use case
  - Modifies: Update endpoint (requires If-Unmodified-Since header validation)
  - Modifies: Error responses (HTTP 409 includes current record state)

- **Interface and contract impact**:
  - HTTP: PUT requests must include `If-Unmodified-Since` header
  - HTTP: GET/POST responses include `Last-Modified` header
  - HTTP: 409 Conflict response includes current record state
  - Error schema: Uses TREQ-0010 with "CONFLICT" error code

- **Backward compatibility**: N/A (new project)

- **Data and migration impact**: N/A

- **Operational impact**:
  - Monitoring: Track 409 conflict rate (high rate indicates contention)
  - Logging: Log conflicts for debugging concurrency issues
  - Client education: Document If-Unmodified-Since requirement

- **Security and compliance impact**:
  - Prevents data loss from concurrent updates (integrity)
  - Audit trail includes who modified (via updatedBy)
  - No additional security concerns

- **Testing impact**:
  - Unit tests: Concurrency check logic
  - Integration tests: Verify 409 returned when modification detected
  - E2E tests: Concurrent update scenarios (two clients, race condition)

- **Traceability impact**:
  - TREQ-0003 (email entity includes updated timestamp)
  - TREQ-0004 (PUT endpoint updated to include If-Unmodified-Since requirement)
  - TREQ-0010 (error schema used for 409 response)
  - US-0003 (update user story now includes conflict handling)

## Recommendation
- **Proposed best option**: Option A — Optimistic Concurrency via If-Unmodified-Since
- **Why**: 
  - HTTP standard (RFC 7232); recognized by all REST developers
  - Stateless (scales horizontally; no session state)
  - Already using `updated` timestamp in entity (TREQ-0003)
  - Aligns with REST conventions (no custom locking semantics)
  - Works well for email records (low contention expected)
  - Enables client-side reconciliation (smart conflict handling)
- **Final decision owner**: Requester

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|-----------|
| Timestamp precision mismatch (headers vs. storage) | Medium | Low | Convert HTTP dates to UTC ms; compare at second precision; document in adapter |
| Clients ignore If-Unmodified-Since (don't read Last-Modified) | Medium | Medium | API documentation emphasizes requirement; return 400 if header missing; client libraries help |
| High conflict rate (many 409 responses) | Low | Low | Monitor conflict rate; if high, may indicate design issue (too much contention) |
| Client doesn't handle 409 gracefully | Low | Medium | Document reconciliation pattern; provide example client code |

## Consistency Exception Assessment
- Exception needed: No
- If-Unmodified-Since aligns with HTTP standards and REST conventions

## Validation
- Requester validation required: No (Option A recommended and approved)
- Validation status: Approved ✓ (2026-05-07)
- Requester selected option: Option A — Optimistic Concurrency via If-Unmodified-Since

## Notes
- **Client responsibility**: Clients must read `Last-Modified` header from read response and include as `If-Unmodified-Since` in update request
- **Reconciliation flow**: If 409 received, client should fetch current state (using updated timestamp from 409 response) and present diff to user
- **Header precedence**: If both `If-Unmodified-Since` and entity in request body specify different timestamps, header takes precedence (HTTP spec)
- **Future enhancement**: If soft-delete implemented, soft-deleted records also checked for concurrency (prevents resurrection conflicts)

## Architecture Best Practices Compliance

- **Separation of concerns**: ✓ Concurrency check isolated in update handler; domain entity unaware of concurrency
- **Coupling/cohesion**: ✓ Low coupling (uses HTTP standard headers); high cohesion (concurrency check focused on one thing)
- **Clear module boundaries and contracts**: ✓ Header contract clearly defined; error response follows TREQ-0010
- **Testability and observability**: ✓ Concurrency check testable; conflict rate monitorable
- **Security-by-design**: ✓ Prevents data loss (integrity); audit trail preserved
- **Maintainability and evolvability**: ✓ HTTP standard (not proprietary); easy to extend if more sophisticated concurrency needed
