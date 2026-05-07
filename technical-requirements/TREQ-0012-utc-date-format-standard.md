# TREQ-0012 - UTC Date Format Standard

## Metadata
- ID: TREQ-0012
- Status: Approved
- Created: 2026-05-07
- Updated: 2026-05-07
- Author Agent: Software Architect
- Source User Stories: US-0001, US-0002, US-0003, US-0004, US-0005
- Related IDs: TREQ-0004, TREQ-0010, TREQ-0011
- Source Links: [technical-requirements/TREQ-0004-email-record-crud-api-endpoints.md](TREQ-0004-email-record-crud-api-endpoints.md)
- Architecture Links: [docs/architecture/architecture-overview.md](../docs/architecture/architecture-overview.md)

## Technical Requirement Statement
The system shall use UTC (Coordinated Universal Time) as the universal timezone for all timestamps. All timestamps in JSON response bodies shall be formatted as ISO 8601 strings with UTC designation (Z suffix). All timestamps in HTTP headers shall be formatted as RFC 7231 dates. Error response timestamps shall follow ISO 8601 UTC format. Internal timestamp storage shall use UTC with millisecond precision. Clients shall never assume local timezone; all timestamps received from the API are in UTC.

## Constraints
- **JSON response bodies**: ISO 8601 UTC format with Z suffix (e.g., `2026-05-07T14:30:00Z` or `2026-05-07T14:30:00.123Z`)
- **HTTP headers**: RFC 7231 format (e.g., `Wed, 07 May 2026 14:30:00 GMT`)
- **Error responses**: ISO 8601 UTC format in timestamp field (e.g., `2026-05-07T14:30:00Z`)
- **Internal storage**: UTC with millisecond precision (no timezone offset; always UTC)
- **All timestamps**: Must explicitly indicate UTC; never omit timezone designation
- **Logs and audit**: Timestamps must be ISO 8601 UTC with Z suffix
- **Clients**: Must parse all timestamps as UTC; no timezone conversion needed

## Existing Coverage Check
- Similar TREQ checked: TREQ-0004, TREQ-0011 mention timestamp handling
- Already covered: Timestamp format partially mentioned in TREQ-0004 notes; not formalized as standard
- Gap: No centralized UTC requirement; examples needed to be consistent

## Current Architecture Baseline
- Existing stack and patterns considered: REST API (TREQ-0004), concurrency control (TREQ-0011), error responses (TREQ-0010)
- Related approved TREQs: TREQ-0004 (endpoints), TREQ-0010 (errors), TREQ-0011 (concurrency)
- Consistency expectation for this decision: All timestamps across all layers must be UTC

## Timestamp Format Reference

### JSON Response Bodies (ISO 8601 UTC)
**Format**: `YYYY-MM-DDTHH:mm:ss.fffZ` or `YYYY-MM-DDTHH:mm:ssZ`

**Examples**:
- Without milliseconds: `2026-05-07T14:30:00Z`
- With milliseconds: `2026-05-07T14:30:00.123Z`
- Midnight UTC: `2026-05-07T00:00:00Z`
- End of day UTC: `2026-05-07T23:59:59.999Z`

**Where used**:
- Domain entity timestamps: `created`, `updated` fields (internal, audit)
- List response fields: `lastModified` field (ISO 8601 UTC)
- Error response timestamps: `timestamp` field (ISO 8601 UTC)
- Audit log timestamps: all audit events

### HTTP Headers (RFC 7231)
**Format**: `ddd, DD MMM YYYY HH:mm:ss GMT`

**Examples**:
- `Wed, 07 May 2026 14:30:00 GMT`
- `Thu, 08 May 2026 09:15:30 GMT`
- `Mon, 01 May 2026 00:00:00 GMT`

**Where used**:
- `Last-Modified` header: response header on GET/POST/PUT
- `If-Unmodified-Since` header: request header on PUT (for concurrency control)
- `Date` header: standard HTTP response header

### Error Responses (ISO 8601 UTC)
**Format**: Same as JSON response bodies — `YYYY-MM-DDTHH:mm:ssZ`

**Example**:
```json
{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Authentication required",
    "timestamp": "2026-05-07T14:30:00Z"
  }
}
```

### Internal Storage (ISO 8601 UTC with milliseconds)
**Format**: `YYYY-MM-DDTHH:mm:ss.fffZ` (always UTC, never local time)

**In-memory representation** (selected stack — Java):
- Use `java.time.Instant` to represent timestamps (UTC by design, nanosecond precision)
- Serialize to ISO 8601 string (`Instant.toString()`) for JSON responses and header conversion
- Never use `java.util.Date` or `java.util.Calendar` (legacy; timezone-unsafe)

## Conversion Rules

### From HTTP Header to Internal Storage
1. Parse RFC 7231 date from header: `Wed, 07 May 2026 14:30:00 GMT`
2. Convert to `Instant` (Java: `Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(headerValue))`)
3. Store as `Instant` in the domain entity
4. Never assume or apply timezone offset (RFC 7231 is always GMT)

**Example**:
```
If-Unmodified-Since: Wed, 07 May 2026 14:30:00 GMT
→ internal: 1430989800000 (ms since epoch)
→ ISO string: 2026-05-07T14:30:00Z
```

### From Internal Storage to HTTP Header
1. Take UTC timestamp (either ISO string or ms)
2. Convert to RFC 7231 format
3. Always use GMT timezone (no offsets)

**Example**:
```
internal: 2026-05-07T14:30:00.123Z
→ RFC 7231: Wed, 07 May 2026 14:30:00 GMT (seconds precision only)
```

### From Internal Storage to JSON Response
1. Take UTC timestamp with milliseconds
2. Serialize as ISO 8601 string with Z suffix
3. Include milliseconds if available; Z suffix mandatory

**Example**:
```
internal: 2026-05-07T14:30:00.123Z
→ response: "2026-05-07T14:30:00.123Z"
```

## Implementation Guidance

### Runtime
- Always obtain timestamps in UTC; never apply local timezone offsets.
- Parse RFC 7231 headers as UTC. Serialize timestamps back to RFC 7231 using GMT suffix (no offset).
- Store and transmit ISO 8601 UTC strings with `Z` suffix; include milliseconds where precision is needed.

### Database
- **PostgreSQL** (recommended): use `TIMESTAMP WITH TIME ZONE`; store and retrieve as UTC.
- **SQLite**: store timestamps as ISO 8601 UTC text strings (`YYYY-MM-DDTHH:mm:ss.fffZ`).

## Anti-Patterns (Avoid)

❌ **Wrong**: `2026-05-07T14:30:00+02:00` (timezone offset applied)
✅ **Correct**: `2026-05-07T14:30:00Z` (explicit UTC)

❌ **Wrong**: `1430989800` (epoch seconds; loses milliseconds)
✅ **Correct**: `1430989800123` (epoch milliseconds) or `2026-05-07T14:30:00.123Z` (ISO string)

❌ **Wrong**: `"2026-05-07 14:30:00"` (no timezone, ambiguous)
✅ **Correct**: `2026-05-07T14:30:00Z` (explicit UTC)

❌ **Wrong**: Storing in local timezone then converting (timezone math errors)
✅ **Correct**: Always store UTC, convert on output only

## Testing Strategy

### Unit Tests
- Parse RFC 7231 headers; verify conversion to UTC
- Serialize ISO 8601 UTC; verify no offset applied
- Round-trip conversions (RFC 7231 → UTC → RFC 7231)
- Millisecond precision preserved in round-trips

### Integration Tests
- API returns ISO 8601 UTC in responses (always Z suffix)
- Last-Modified header is RFC 7231 GMT
- Error timestamps are ISO 8601 UTC
- Concurrency check (If-Unmodified-Since) correctly parses RFC 7231

### Examples
```gherkin
Scenario: Create email record returns UTC timestamps
  Given a user creates an email record
  When the response is returned
  Then the Last-Modified header is RFC 7231 format (e.g., "Wed, 07 May 2026 14:30:00 GMT")
  And the response body lastModified field is ISO 8601 UTC (e.g., "2026-05-07T14:30:00.123Z")

Scenario: Update email with concurrency check
  Given a user reads an email record
  And Last-Modified header is "Wed, 07 May 2026 14:30:00 GMT"
  When the user sends PUT with If-Unmodified-Since: "Wed, 07 May 2026 14:30:00 GMT"
  And the record's internal updated timestamp is "2026-05-07T14:30:00.000Z"
  Then the update succeeds
  And the response Last-Modified is "Wed, 07 May 2026 14:30:01 GMT"
```

## Validation
- Requester validation required: No (UTC standard is universal best practice)
- Validation status: Approved ✓ (2026-05-07)
- Requester selected option: N/A (UTC is mandatory standard)

## Notes
- All timestamps are by definition UTC; no client-side timezone conversion needed
- Millisecond precision improves concurrency control accuracy (two updates in same second can be distinguished)
- RFC 7231 (HTTP header) has second precision; internal storage preserves milliseconds
- Audit logs must also use UTC ISO 8601 format for consistency
- Time zones are never mentioned in API documentation; UTC is implicit

## Architecture Best Practices Compliance

- **Separation of concerns**: ✓ Timestamp format standard independent from business logic; formatters in adapters
- **Coupling/cohesion**: ✓ Low coupling (UTC is universal); high cohesion (all timestamps follow same pattern)
- **Clear module boundaries and contracts**: ✓ Timestamp format is explicit contract between API and clients
- **Testability and observability**: ✓ UTC eliminates timezone ambiguity; logs are universally readable
- **Security-by-design**: ✓ UTC prevents timestamp-based attacks (no timezone confusion)
- **Maintainability and evolvability**: ✓ Standard format simplifies debugging and multi-region support
