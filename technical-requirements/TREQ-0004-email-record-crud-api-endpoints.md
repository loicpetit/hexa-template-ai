# TREQ-0004 - Email Record CRUD API Endpoints

## Metadata
- ID: TREQ-0004
- Status: Approved
- Created: 2026-05-07
- Updated: 2026-05-07
- Author Agent: Software Architect
- Source User Stories: US-0001, US-0002, US-0003, US-0004, US-0005
- Related IDs: REQ-0001, REQ-0003, TREQ-0001, TREQ-0010, TREQ-0011
- Source Links: [requirements/REQ-0001-email-crud-api.md](../requirements/REQ-0001-email-crud-api.md), [requirements/REQ-0003-email-record-response-fields-contract.md](../requirements/REQ-0003-email-record-response-fields-contract.md)
- Architecture Links: [docs/architecture/architecture-overview.md](../docs/architecture/architecture-overview.md), [technical-requirements/TREQ-0010-http-error-response-schema.md](TREQ-0010-http-error-response-schema.md), [technical-requirements/TREQ-0011-optimistic-concurrency-control-if-unmodified-since.md](TREQ-0011-optimistic-concurrency-control-if-unmodified-since.md)

## Technical Requirement Statement
The system shall provide HTTP REST API endpoints for creating, reading, updating, and hard-deleting email records. Each endpoint shall require authentication (HTTP Authorization header with bearer token). All endpoints shall return responses with consistent JSON schema including only public fields (id, value), except list operations which also include updated timestamp. Internal metadata (created, createdBy, updatedBy) shall not be exposed in response bodies and shall be used for audit purposes only. Single-record operations (create, read, update) shall include Last-Modified header with RFC 7231 formatted updated timestamp to enable optimistic concurrency control via If-Unmodified-Since request header (TREQ-0011). Endpoint errors shall return appropriate HTTP status codes and error descriptions per TREQ-0010.

## Constraints
- All endpoints must require authentication (401 Unauthorized if token missing/invalid).
- All endpoints must accept JSON request bodies (for create/update operations).
- Single-record endpoints (create, read by id, update) return only id and value in response body.
- List endpoint returns id, value, and updated in response body (for each record).
- All successful single-record responses must include Last-Modified header with RFC 7231 formatted updated timestamp (for concurrency control).
- Internal metadata (created, createdBy, updatedBy) must not be exposed in response bodies.
- Create response must return HTTP 201 Created.
- Read response must return HTTP 200 OK.
- List response must return HTTP 200 OK with array of records.
- Update response must return HTTP 200 OK.
- Delete response must return HTTP 204 No Content.
- Error responses must include status code and error message (see TREQ-0010).
- Request/response payloads must not include sensitive data (e.g., auth tokens, internal ids in non-standard ways).

## Existing Coverage Check
- Similar TREQ checked: None (first API TREQ)
- Already covered: REQ-0001 specifies CRUD operations; REQ-0003 specifies response fields; technical HTTP contract not specified
- Gap: Requirements define what to expose; TREQ specifies HTTP methods, status codes, request/response schema

## Current Architecture Baseline
- Existing stack and patterns considered: Hexagonal architecture (TREQ-0001) defines REST adapter layer
- Related approved TREQs: TREQ-0001 (primary adapter layer), TREQ-0003 (EmailRecordDTO)
- Consistency expectation for this decision: Endpoints map REST HTTP to use cases; adapters translate between HTTP and domain

## Technical Module Organization

### Primary Adapter: REST API
**Location**: `adapters/primary/rest-api`  
**Responsibility**: HTTP endpoint handling, request/response serialization, error mapping

### Endpoint Specifications

#### 1. Create Email Record
**HTTP Method & Path**: `POST /emails`  
**Authentication**: Required (Bearer token)

**Request**:
```json
{
  "value": "user@example.com"
}
```

**Request Validation**:
- `value` is required, must be non-empty string, valid email format
- Invalid request → HTTP 400 Bad Request

**Response** (HTTP 201 Created):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "value": "user@example.com"
}
```

Response headers:
```
Last-Modified: Wed, 07 May 2026 14:30:00 GMT
```

**Error Responses** (see TREQ-0010 for unified error response schema):
- Missing token → HTTP 401 Unauthorized: `{ "error": { "code": "UNAUTHORIZED", "message": "Authentication required", "timestamp": "2026-05-07T14:30:00Z" } }`
- Invalid token → HTTP 401 Unauthorized: `{ "error": { "code": "UNAUTHORIZED", "message": "Invalid or expired token", "timestamp": "2026-05-07T14:30:00Z" } }`
- Invalid email format → HTTP 400 Bad Request: `{ "error": { "code": "INVALID_EMAIL_FORMAT", "message": "Invalid email format", "timestamp": "2026-05-07T14:30:00Z" } }`
- Invalid request body → HTTP 400 Bad Request: `{ "error": { "code": "INVALID_REQUEST", "message": "Missing required field: value", "timestamp": "2026-05-07T14:30:00Z" } }`

**Use Case**: CreateEmailRecord (orchestrates domain entity creation, audit capture, repository save)

---

#### 2. Read Single Email Record
**HTTP Method & Path**: `GET /emails/{id}`  
**Authentication**: Required (Bearer token)

**Path Parameters**:
- `id` (required): Email record identifier

**Response** (HTTP 200 OK):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "value": "user@example.com"
}
```

Response headers:
```
Last-Modified: Wed, 07 May 2026 14:30:00 GMT
```

---

#### 3. List All Email Records
**HTTP Method & Path**: `GET /emails`  
**Authentication**: Required (Bearer token)

**Query Parameters** (Baseline — no filters or sorting):
- None in baseline (future enhancement: add filtering, sorting, pagination)

**Response** (HTTP 200 OK):
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "value": "user@example.com",
    "lastModified": "2026-05-07T14:30:00Z"
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440111",
    "value": "another@example.com",
    "lastModified": "2026-05-07T14:45:00Z"
  }
]
```

Response headers:
```
Last-Modified: Wed, 07 May 2026 14:45:00 GMT
```
(Last-Modified reflects the most recent record's updated timestamp)

**Error Responses** (see TREQ-0010 for unified error response schema):
- Missing token → HTTP 401 Unauthorized: `{ "error": { "code": "UNAUTHORIZED", "message": "Authentication required", "timestamp": "2026-05-07T14:30:00Z" } }`
- Invalid token → HTTP 401 Unauthorized: `{ "error": { "code": "UNAUTHORIZED", "message": "Invalid or expired token", "timestamp": "2026-05-07T14:30:00Z" } }`

**Use Case**: ListEmailRecords (orchestrates repository query all, returns array of DTOs)

---

#### 4. Update Email Record
**HTTP Method & Path**: `PUT /emails/{id}`  
**Authentication**: Required (Bearer token)
**Concurrency Control**: Required (See TREQ-0011 for details)

**Path Parameters**:
- `id` (required): Email record identifier

**Headers** (required):
- `If-Unmodified-Since` (required): RFC 7231 date matching email record's last known `updated` timestamp (e.g., `Wed, 07 May 2026 14:30:00 GMT`). See TREQ-0011 for concurrency control details.

**Request**:
```json
{
  "value": "newemail@example.com"
}
```

**Request Validation**:
- `value` is required, must be non-empty string, valid email format
- `If-Unmodified-Since` header is required; missing → HTTP 400 Bad Request
- Invalid request → HTTP 400 Bad Request

**Response** (HTTP 200 OK — update succeeded):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "value": "newemail@example.com"
}
```

Response headers:
```
Last-Modified: Wed, 07 May 2026 14:45:00 GMT
```

**Response** (HTTP 409 Conflict — record modified since If-Unmodified-Since timestamp):
- See TREQ-0011 for detailed conflict response format
- Includes current record state for client reconciliation

**Error Responses**:
- Missing token → HTTP 401 Unauthorized
- Invalid token → HTTP 401 Unauthorized
- Record not found → HTTP 404 Not Found
- Invalid email format → HTTP 400 Bad Request
- Missing If-Unmodified-Since header → HTTP 400 Bad Request
- Record modified (concurrency conflict) → HTTP 409 Conflict (see TREQ-0011)
- Other errors → See TREQ-0010 for unified error response schema

**Use Case**: UpdateEmailRecord (orchestrates concurrency check, repository query, domain entity update, audit capture, repository save)

---

#### 5. Delete Email Record (Hard Delete)
**HTTP Method & Path**: `DELETE /emails/{id}`  
**Authentication**: Required (Bearer token)

**Path Parameters**:
- `id` (required): Email record identifier

**Response** (HTTP 204 No Content):
- Empty body (no content returned on successful deletion)

**Error Responses** (see TREQ-0010 for unified error response schema):
- Missing token → HTTP 401 Unauthorized: `{ "error": { "code": "UNAUTHORIZED", "message": "Authentication required", "timestamp": "2026-05-07T14:30:00Z" } }`
- Invalid token → HTTP 401 Unauthorized: `{ "error": { "code": "UNAUTHORIZED", "message": "Invalid or expired token", "timestamp": "2026-05-07T14:30:00Z" } }`

**Use Case**: HardDeleteEmailRecord (orchestrates repository deletion, returns confirmation)

---

## Response Schema (Shared by All Endpoints)

### Success Response: Email Record DTO

**Single-Record DTO (POST, GET by id, PUT):**

| Field | Type | Notes |
|-------|------|-------|
| `id` | string | UUID v4 (public) |
| `value` | string | Email address (public) |

Internal metadata (`created`, `createdBy`, `updated`, `updatedBy`) is NOT exposed. The `Last-Modified` response header carries the updated timestamp for concurrency control.

**List Record DTO (GET all):**

| Field | Type | Notes |
|-------|------|-------|
| `id` | string | UUID v4 (public) |
| `value` | string | Email address (public) |
| `lastModified` | string | ISO 8601 UTC timestamp (public in list only; semantic match to `Last-Modified` header) |

Internal metadata (`created`, `createdBy`, `updatedBy`) is NOT exposed.

### Response Headers
All successful responses include:
- `Last-Modified: <RFC 7231 date>` — Matches the email record's `updated` timestamp (for concurrency control; see TREQ-0011)

### Error Response Schema
See TREQ-0010 for unified HTTP error response schema used by all endpoints.

---

## HTTP Status Code Mapping

| Scenario | Status Code | Reason |
|----------|-------------|--------|
| Create success | 201 Created | New resource created |
| Read success | 200 OK | Resource retrieved |
| List success | 200 OK | Resources retrieved |
| Update success | 200 OK | Resource updated |
| Delete success | 204 No Content | Resource deleted, no content returned |
| Missing authentication | 401 Unauthorized | No valid token provided |
| Invalid token | 401 Unauthorized | Token invalid or expired |
| Record not found | 404 Not Found | Requested resource doesn't exist |
| Bad request (validation fail) | 400 Bad Request | Invalid input format |
| Internal error | 500 Internal Server Error | Unexpected server error |

---

## Alternatives

### Option A: REST API (Proposed)
**Approach**: Standard REST HTTP endpoints with JSON payloads and status codes
- Endpoints: POST, GET (single), GET (list), PUT, DELETE
- Status codes: 200, 201, 204, 400, 401, 404, 500
- Media type: application/json

**Pros**:
- Widely understood and supported
- Standard HTTP conventions
- Easy to test (curl, Postman, etc.)
- Compatible with REST clients in all languages

**Cons**:
- Limited by HTTP semantics (can't express complex queries without query params)
- No built-in versioning (must use header or URL path)

**Consistency impact**: Industry standard for REST APIs

---

### Option B: GraphQL API (Alternative)
**Approach**: Query language endpoint; clients request specific fields
- Single endpoint: POST /graphql
- Queries for read; mutations for create/update/delete
- Response includes only requested fields

**Pros**:
- Flexible field selection (no over-fetching)
- Built-in type system and schema
- Single network round-trip for complex operations
- Easier versioning (schema evolution)

**Cons**:
- More complex to implement (GraphQL server)
- Steeper learning curve (for clients)
- Overkill for simple CRUD
- Debugging can be harder

**Consistency impact**: Modern but adds operational complexity for simple operations

---

### Option C: RPC API (gRPC or Custom) (Alternative)
**Approach**: Procedure-based API; structured binary protocol
- Methods: CreateEmail, ReadEmail, ListEmails, UpdateEmail, DeleteEmail
- Protocol: gRPC (protobuf) or custom binary
- Encoding: Binary (not JSON)

**Pros**:
- Faster (binary encoding, multiplexing)
- Strong schema (protobuf definitions)
- Language-agnostic code generation

**Cons**:
- Harder to test (need special tools)
- Less browser-friendly (not HTTP/REST)
- Overkill for CRUD scope

**Consistency impact**: Adds operational complexity without clear benefit

---

## Top 3 Ranking

1. **Option A: REST API (Proposed)**
   - Why: Industry standard for HTTP APIs. Simple to implement and test. Well-understood by all developers. Sufficient for CRUD operations. RESTful conventions guide design.

2. **Option B: GraphQL API**
   - Why: More flexible if diverse clients with different field requirements emerge. Better for future complex queries. Consider if project scope increases significantly.

3. Option C: gRPC API
   - Why: Not recommended for this scope. Better for high-throughput, low-latency services or microservice communication.

## Trade-Offs

- **Simplicity vs. Flexibility**: REST is simple but less flexible than GraphQL. Trade accepted: simplicity appropriate for baseline CRUD.

- **Standardization vs. Custom logic**: REST follows HTTP conventions (POST=create, PUT=update). Trade accepted: conventions reduce decision-making.

- **Network efficiency vs. Ease of testing**: REST/JSON adds overhead but is easier to test than binary protocols. Trade accepted: testability over network efficiency for business CRUD.

## Impact Analysis

- **Functional impact**:
  - All CRUD operations available via HTTP endpoints
  - Consistent response format (all operations return EmailRecordDTO)
  - Authentication enforced on all endpoints
  - Proper HTTP status codes guide clients

- **Module impact**:
  - Adds: Primary REST adapter (endpoint handlers)
  - Adds: Request/response serializers
  - Adds: HTTP error mappers
  - Modifies: Use case handlers (receive deserialize requests)

- **Interface and contract impact**:
  - External: 5 HTTP endpoints (POST, GET, GET list, PUT, DELETE)
  - Internal: Use cases receive deserialized requests; return DTOs
  - Schema: EmailRecordDTO, ErrorResponse

- **Backward compatibility**: N/A (new project)

- **Data and migration impact**: N/A (new project)

- **Operational impact**:
  - HTTP server listens on port (default 3000, configurable)
  - Endpoints documented in OpenAPI/Swagger (optional, recommended)
  - Monitoring: Track request count, latency, errors per endpoint

- **Security and compliance impact**:
  - HTTPS enforced (deployment concern, not in this TREQ)
  - Authentication required (bearer token validation)
  - Rate limiting recommended (future concern)
  - Error responses sanitized (no internal stack traces)

- **Testing impact**:
  - Unit tests: Endpoint handlers tested with mock use cases
  - Integration tests: Full HTTP requests through adapters
  - E2E tests: Real HTTP requests to deployed API
  - Test data: Fixtures for all 5 endpoints

- **Traceability impact**:
  - REQ-0001 (CRUD operations) → TREQ-0004 (HTTP endpoints)
  - US-0001 through US-0004 → TREQ-0004 (each endpoint implements one use case)
  - E2E tests verify endpoints return correct status codes and responses

## Recommendation
- **Proposed best option**: Option A — REST API
- **Why**: 
  - Aligns with HTTP standards and RESTful conventions
  - Simple to implement and test
  - Well-understood by all developers
  - Sufficient for current CRUD scope
  - Easy to extend in future (add query params for filtering, pagination, etc.)
- **Final decision owner**: Requester

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|-----------|
| Clients assume response status codes follow conventions | Low | Low | Document status code mappings clearly; return standard HTTP codes |
| Request validation inconsistent across endpoints | Medium | Low | Centralize validation logic in middleware or decorator |
| Error messages expose internal details | Low | High | Sanitize error responses; log full details server-side |
| Endpoint discovery difficult for clients | Low | Low | Generate OpenAPI/Swagger documentation (optional but recommended) |
| Timestamp format misunderstood | Low | Low | Use ISO 8601 string format consistently; document in API spec |

## Consistency Exception Assessment
- Exception needed: No
- REST API aligns with HTTP standards and hexagonal architecture (primary adapter layer)

## Validation
- Requester validation required: No (REST API is recommended baseline)
- Validation status: Approved ✓ (2026-05-07)
- Requester selected option: Option A — REST API; If-Unmodified-Since for concurrency control; unified error schema (TREQ-0010)

## Notes
- All timestamps serialized as ISO 8601 strings (e.g., "2026-05-07T14:30:00Z") for JSON compatibility.
- User ids in createdBy/updatedBy are strings (e.g., "user-123") for privacy; full user details not returned.
- DELETE returns 204 No Content (standard HTTP; no body returned) rather than 200 OK with deleted object.
- List endpoint returns empty array [] if no records exist (not null or error).
- API versioning (v1, v2, etc.) not included in baseline but can be added if needed (e.g., /api/v1/emails or Accept-Version header).

## Architecture Best Practices Compliance

- **Separation of concerns**: ✓ Primary adapter handles HTTP; use cases handle business logic; domain handles validation
- **Coupling/cohesion**: ✓ Endpoints loosely coupled (via use case interfaces); highly cohesive (each endpoint has single responsibility)
- **Clear module boundaries and contracts**: ✓ Request/response schemas defined; endpoint responsibilities clear; error codes standard
- **Testability and observability**: ✓ Endpoints testable with mock use cases; request/response logged; errors traced
- **Security-by-design**: ✓ Authentication enforced; error responses sanitized; no internal details exposed
- **Maintainability and evolvability**: ✓ REST conventions guide design; new endpoints easy to add; schema-driven development possible (OpenAPI)
