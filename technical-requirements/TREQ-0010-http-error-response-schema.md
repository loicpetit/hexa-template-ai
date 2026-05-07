# TREQ-0010 - HTTP Error Response Schema

## Metadata
- ID: TREQ-0010
- Status: Approved
- Created: 2026-05-07
- Updated: 2026-05-07
- Author Agent: Software Architect
- Source User Stories: US-0001, US-0002, US-0003, US-0004, US-0005
- Related IDs: TREQ-0004
- Source Links: [technical-requirements/TREQ-0004-email-record-crud-api-endpoints.md](TREQ-0004-email-record-crud-api-endpoints.md)
- Architecture Links: [docs/architecture/architecture-overview.md](../docs/architecture/architecture-overview.md)

## Technical Requirement Statement
The system shall return HTTP error responses with a consistent, machine-readable schema across all REST endpoints. Error responses shall include status code, error message, error code (for programmatic handling), and timestamp. The error schema shall be framework-independent and reusable across current and future endpoints, enabling clients to parse errors consistently.

## Constraints
- All error responses must use the same JSON schema (consistency across endpoints).
- Error responses must include: HTTP status code, error message (human-readable), error code (machine-readable), timestamp.
- Error message must not expose internal implementation details (e.g., stack traces, database query info).
- Error code must be stable (not change between versions; enables client-side error handling).
- Timestamp must be ISO 8601 format (UTC, consistent with success responses).
- Status code must follow HTTP conventions (400 for client errors, 500 for server errors, etc.).

## Existing Coverage Check
- Similar TREQ checked: None (error response schema not yet specified independently)
- Already covered: TREQ-0004 includes individual error examples; schema not formalized for reuse
- Gap: Error responses currently scattered across endpoint docs; need unified contract

## Current Architecture Baseline
- Existing stack and patterns considered: REST API (TREQ-0004) returns errors
- Related approved TREQs: TREQ-0004 (REST endpoints)
- Consistency expectation for this decision: All endpoints return errors with same schema

## Error Response Schema

### Standard Error Response
```
HTTP/<status> 
Content-Type: application/json

{
  "error": {
    "code": "<ERROR_CODE>",
    "message": "<human-readable message>",
    "timestamp": "2026-05-07T14:35:22Z"
  }
}
```

**Field Definitions**:
- **code**: Machine-readable error identifier (e.g., "INVALID_EMAIL_FORMAT", "RESOURCE_NOT_FOUND")
- **message**: Human-readable error description (for UI display or logging)
- **timestamp**: ISO 8601 UTC timestamp when error occurred (for correlation with server logs)

### Standard Error Codes & HTTP Status Mappings

| HTTP Status | Error Code | Use Case | Retry? |
|-------------|-----------|----------|--------|
| 400 | INVALID_REQUEST | Request validation fails (missing field, bad format) | No |
| 400 | INVALID_EMAIL_FORMAT | Email value fails validation | No |
| 401 | UNAUTHORIZED | Missing or invalid authentication token | No (maybe after refresh) |
| 404 | RESOURCE_NOT_FOUND | Requested email record doesn't exist | No |
| 409 | CONFLICT | Optimistic concurrency check fails (If-Unmodified-Since) | Yes |
| 500 | INTERNAL_ERROR | Unexpected server error | Yes (exponential backoff) |

### Error Response Examples

**Example 1: Validation Error (400)**
```json
{
  "error": {
    "code": "INVALID_EMAIL_FORMAT",
    "message": "Email address format is invalid",
    "timestamp": "2026-05-07T14:35:22Z"
  }
}
```

**Example 2: Not Found (404)**
```json
{
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Email record with id 550e8400 not found",
    "timestamp": "2026-05-07T14:35:22Z"
  }
}
```

**Example 3: Concurrency Conflict (409)**
```json
{
  "error": {
    "code": "CONFLICT",
    "message": "Email record was modified since last read; update rejected",
    "timestamp": "2026-05-07T14:35:22Z"
  }
}
```

**Example 4: Server Error (500)**
```json
{
  "error": {
    "code": "INTERNAL_ERROR",
    "message": "An unexpected error occurred. Please contact support.",
    "timestamp": "2026-05-07T14:35:22Z"
  }
}
```

---

## Design Rationale

**Why this schema?**
- **code**: Enables client-side error handling (if code === "RESOURCE_NOT_FOUND", show 404 page)
- **message**: Human-readable for logging, debugging, and UI display
- **timestamp**: Correlates errors with server logs for troubleshooting

**Why no additional fields?**
- Simplicity: fewer fields reduce parsing complexity
- Security: no stack traces or internal details exposed
- Future-proof: fields can be added without breaking existing clients (JSON is flexible)

**Why not wrapped in array?**
- Single error per response (API returns one error at a time; HTTP status code indicates type)
- If bulk validation needed in future, schema can evolve (e.g., "errors": [...])

---

## Alternatives

### Option A: Unified Error Schema (Proposed)
- Single JSON structure for all errors
- Fields: code, message, timestamp
- Consistent across all endpoints

**Pros**: Simplicity, consistency, easy to parse, clear contract

**Cons**: May feel restrictive if complex errors needed in future

---

### Option B: Detailed Error Schema with Metadata
- Include additional fields: request_id, help_url, retry_after
- Support structured field-level validation errors

**Pros**: More context for debugging; request correlation; helpful links

**Cons**: More complex to parse; larger payloads; harder to maintain consistency

---

### Option C: HTTP Status Code Only
- Return only status code; no error body
- Rely on status codes (400, 401, 404, 500) for error info

**Pros**: Ultra-minimal; no parsing needed

**Cons**: No human-readable message; can't distinguish between similar errors (e.g., which field failed validation?)

---

## Top 3 Ranking

1. **Option A: Unified Error Schema (Proposed)**
   - Why: Simple, consistent, covers 99% of use cases. Easy to document and test. Scales to future endpoints.

2. **Option B: Detailed Error Schema**
   - Why: Better for large APIs with many endpoints. Consider if error context becomes critical.

3. **Option C: HTTP Status Code Only**
   - Why: Too minimal; not recommended. Clients can't display meaningful error messages.

## Trade-Offs

- **Simplicity vs. Context**: Option A simpler; Option B more context. Trade: Simplicity chosen for POC.

- **Extensibility vs. Rigidity**: Option A minimal (can extend); Option B comprehensive (hard to change). Trade: Minimal now; add fields as needed.

## Impact Analysis

- **Functional impact**: None — error responses don't change API behavior; they communicate failures

- **Module impact**:
  - Adds: Error response mapper (translates domain exceptions to HTTP error schema)
  - Modifies: All endpoint handlers (use mapper to format errors)

- **Interface and contract impact**:
  - HTTP: All 4xx and 5xx responses use unified schema
  - Clients can parse any error uniformly

- **Backward compatibility**: N/A (new project)

- **Data and migration impact**: N/A

- **Operational impact**:
  - Error responses logged for debugging (timestamp for correlation)
  - Error codes enable monitoring (alert if error rate > threshold)

- **Security and compliance impact**:
  - No sensitive data exposed (sanitized messages)
  - Timestamp enables audit trail

- **Testing impact**:
  - Unit tests verify all error paths return correct schema
  - E2E tests verify error responses match schema

- **Traceability impact**:
  - TREQ-0004 references TREQ-0010 for error schema
  - Future endpoints reference TREQ-0010 (avoid duplicating error docs)

## Recommendation
- **Proposed best option**: Option A — Unified Error Schema
- **Why**: 
  - Simple, consistent, easy to implement and test
  - Covers all current use cases
  - Scalable to future endpoints (reusable contract)
  - Clients have predictable error handling pattern
- **Final decision owner**: Requester

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|-----------|
| Error message too generic (not helpful) | Low | Low | Document error codes clearly; include suggestion in message (e.g., "invalid email format; expected user@domain.com") |
| Clients expect different error format | Low | Low | API documentation clarifies schema; client libraries handle parsing |
| Schema too rigid (can't add fields later) | Low | Low | JSON extensible; add fields without breaking existing clients |

## Consistency Exception Assessment
- Exception needed: No
- Unified error schema aligns with REST API conventions and hexagonal architecture

## Validation
- Requester validation required: No (Option A recommended and approved)
- Validation status: Approved ✓ (2026-05-07)
- Requester selected option: Option A — Unified Error Schema

## Notes
- Error codes follow SCREAMING_SNAKE_CASE convention (consistent, scannable)
- All timestamps UTC (no timezone ambiguity)
- Error messages are one-liners (no multi-line text; clients parse JSON, not text)
- For future: If detailed field-level validation errors needed (e.g., multiple fields invalid), schema can evolve to include optional "details" field with array of field errors

## Architecture Best Practices Compliance

- **Separation of concerns**: ✓ Error schema defined separately; reusable across endpoints
- **Coupling/cohesion**: ✓ Low coupling (clients depend on schema, not implementation); high cohesion (schema focused on error communication)
- **Clear module boundaries and contracts**: ✓ Error schema is explicit contract between API and clients
- **Testability and observability**: ✓ Error codes enable automated testing and monitoring
- **Security-by-design**: ✓ No sensitive data in errors; timestamp for audit
- **Maintainability and evolvability**: ✓ Schema centralized in TREQ; can be updated once and propagates to all endpoints
