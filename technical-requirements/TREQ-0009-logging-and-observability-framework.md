# TREQ-0009 - Logging & Observability Framework

## Metadata
- ID: TREQ-0009
- Status: Approved
- Created: 2026-05-07
- Updated: 2026-05-07
- Author Agent: Software Architect
- Source User Stories: US-0001, US-0002, US-0003, US-0004, US-0005
- Related IDs: TREQ-0007, TREQ-0005 (audit logging uses same framework)
- Source Links: [technical-requirements/TREQ-0005-audit-attribution-system.md](TREQ-0005-audit-attribution-system.md)
- Architecture Links: [docs/architecture/architecture-overview.md](../docs/architecture/architecture-overview.md)

## Technical Requirement Statement
The system shall use a structured logging framework to record application events, errors, and performance data. Logs must be machine-readable (JSON format) and queryable, supporting troubleshooting and compliance auditing. The logging framework must be configurable by environment (debug in development, warnings/errors in production) and must not impact application performance significantly. The audit logging system (TREQ-0005) shall use the same framework infrastructure.

## Constraints
- Logging framework must support structured output (JSON, not plain text).
- Must support different log levels (debug, info, warn, error, fatal).
- Must support adding contextual metadata to logs (request id, user id, transaction id for correlation).
- Must support asynchronous appenders (logging must not block application logic).
- Must support log rotation and retention policies (important for production).
- Must integrate natively with Java and Spring Boot (first-class Java support).
- Must have minimal startup overhead (fast application boot time).
- Framework must not enforce application structure (loggers can be called from any layer via SLF4J facade).

## Existing Coverage Check
- Similar TREQ checked: None (logging framework not yet specified)
- Already covered: TREQ-0005 mentions audit logging but not framework chosen
- Gap: Logging strategy defined (structured, machine-readable); framework/tool not specified

## Current Architecture Baseline
- Existing stack and patterns considered: TREQ-0007 (Java 25 + Spring Boot 4.x + Gradle)
- Related approved TREQs: TREQ-0005 (audit events use same framework)
- Consistency expectation for this decision: Framework must integrate with Spring Boot auto-configuration; must use SLF4J as the logging facade so application code is decoupled from the underlying implementation

## Alternatives

### Option A: SLF4J + Logback (Spring Boot default)
**Profile**: De-facto standard logging stack for Java; Spring Boot auto-configures Logback by default
- **Output**: JSON (via `logstash-logback-encoder`), plain text, or custom patterns
- **Levels**: trace, debug, info, warn, error
- **Features**: Rolling file appenders, MDC (contextual metadata), async appenders, Spring Boot integration

**Pros**:
- Zero setup — Spring Boot auto-configures Logback out of the box
- SLF4J facade decouples application code from implementation (swap implementation without touching code)
- `logstash-logback-encoder` library adds production-grade JSON output
- MDC (Mapped Diagnostic Context) natively supports request id / user id correlation
- Async appenders available (non-blocking)
- Largest Java community; extensive documentation

**Cons**:
- JSON output requires `logstash-logback-encoder` dependency (not built-in)
- Logback configuration via XML (`logback-spring.xml`) can feel verbose

**Consistency impact**: Fully aligned — Spring Boot default; no additional dependency management overhead

---

### Option B: SLF4J + Log4j2
**Profile**: High-performance alternative to Logback; same SLF4J facade
- **Output**: JSON layout built-in; multiple appender types
- **Levels**: trace, debug, info, warn, error, fatal
- **Features**: Async logging (LMAX Disruptor), lazy evaluation, plugin system

**Pros**:
- Higher throughput than Logback for async logging (LMAX Disruptor architecture)
- JSON layout built-in (no extra library needed)
- Plugin system allows deep customization
- Same SLF4J facade — code changes zero when switching from Logback

**Cons**:
- Requires excluding Spring Boot's default Logback dependency
- Slightly more complex setup than Logback
- Past critical CVEs (Log4Shell — Log4j2 2.x; mitigated in 2.17+)

**Consistency impact**: Minor break from Spring Boot default; same SLF4J facade preserves code consistency

---

### Option C: SLF4J + Logback with Logstash Encoder (production-ready JSON variant of Option A)
**Profile**: Same as Option A but with explicit JSON-first configuration from the start

This is not a separate framework — it is Option A with `logstash-logback-encoder` pre-configured. Listed separately to highlight that JSON output must be intentionally configured; it does not appear automatically.

---

## Top 3 Ranking

1. **Option A: SLF4J + Logback** *(recommended)*
   - Why: Spring Boot default; zero extra setup; SLF4J facade keeps all application code framework-agnostic; `logstash-logback-encoder` adds JSON output with minimal effort.

2. **Option B: SLF4J + Log4j2**
   - Why: Higher async throughput; consider if logging performance becomes a bottleneck. Same SLF4J facade means migration is non-breaking.

3. **Option C** is a configuration variant of Option A, not a distinct choice.

If fewer than 3 realistic options exist, list only valid options and explain why.

## Trade-Offs

- **Zero setup vs. performance ceiling**: Logback is simpler to start; Log4j2 has higher async throughput. Trade: Logback appropriate for current scope; Log4j2 is a non-breaking upgrade path.
- **Built-in JSON vs. extra dependency**: Log4j2 has JSON layout built-in; Logback requires `logstash-logback-encoder`. Trade: minor dependency; widely used, actively maintained.
- **Spring Boot alignment vs. customisation**: Logback is the Spring Boot default; Log4j2 requires explicit exclusion of Logback. Trade: Logback keeps stack simpler.

## Impact Analysis

- **Functional impact**: None — logging is a cross-cutting concern; does not change API behaviour

- **Module impact**:
  - Adds: Logger configuration (`logback-spring.xml` or `log4j2-spring.xml`)
  - All layers log via `LoggerFactory.getLogger(...)` (SLF4J); no framework import in domain layer
  - Adds: MDC setup in HTTP adapter (request id, user id injected per request)

- **Interface and contract impact**:
  - Internal: All loggers obtained via SLF4J facade (`org.slf4j.Logger`); implementation is invisible to callers
  - JSON log schema consistent across all modules

- **Backward compatibility**: N/A (new project)

- **Data and migration impact**:
  - Logs written to console (dev) and rolling file (prod); no schema migration
  - Log retention policies configured at deployment level

- **Operational impact**:
  - Rolling file appenders prevent unbounded disk usage
  - Log levels configurable per environment via `application.properties` / environment variables
  - Spring Boot Actuator (`/actuator/loggers`) allows runtime log level changes without restart

- **Security and compliance impact**:
  - Sensitive data (passwords, tokens) must never appear in log messages — enforced by code review
  - Audit events (TREQ-0005) tagged and filterable from application logs
  - Compliance: log retention and immutability handled at deployment level

- **Testing impact**:
  - Unit tests: SLF4J `Logger` easily mocked or captured with test appenders (e.g., `ListAppender`)
  - Integration tests: Capture log output; verify error conditions logged correctly

- **Traceability impact**:
  - TREQ-0005 (audit logging) uses same SLF4J infrastructure
  - Request id correlation links application logs to audit events

## Recommendation
- **Proposed best option**: Option A — SLF4J + Logback with `logstash-logback-encoder`
- **Why**:
  - Spring Boot default — zero setup friction
  - SLF4J facade keeps domain and application layers completely framework-agnostic
  - MDC provides native request id / user id correlation
  - `logstash-logback-encoder` is the standard Java library for structured JSON logging
  - Battle-tested, well-documented, large community
  - Non-breaking upgrade path to Log4j2 if throughput becomes critical
- **Final decision owner**: Requester

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|-----------|
| Sensitive data accidentally logged | Medium | High | Code review; never log request bodies or credentials; MDC cleared after each request |
| Log volume overwhelming disk | Medium | Medium | Rolling file appenders with size/time limits; log aggregation service in production |
| Log4j-style CVE in chosen library | Low | High | Pin library versions; subscribe to CVE alerts; update promptly |
| MDC not cleared between requests (data leakage in thread pool) | Low | Medium | Clear MDC in a servlet filter after each request |

## Consistency Exception Assessment
- Exception needed: No
- SLF4J + Logback is the Spring Boot default and aligns fully with the selected stack

## Validation
- Requester validation required: Yes
- Validation status: Approved
- Requester selected option: A — SLF4J + Logback with `logstash-logback-encoder`

## Notes
- **SLF4J facade**: Application code imports only `org.slf4j.Logger` — never the Logback or Log4j2 classes directly. This is mandatory.
- **JSON configuration**: Add `logstash-logback-encoder` to `build.gradle` and configure `logback-spring.xml` for JSON output in production profile.
- **MDC usage**: HTTP adapter must set `requestId` and `userId` in MDC at the start of each request and clear it after.
- **Audit logging integration**: Audit events (TREQ-0005) logged at INFO level with a dedicated marker (e.g., `AUDIT`) for easy filtering.
- **Development vs. production**: In development, plain text with colours via console appender; in production, JSON to rolling file or stdout for log aggregation.

## Architecture Best Practices Compliance

- **Separation of concerns**: ✓ Logging is cross-cutting; injected via SLF4J facade — never imported directly in domain layer
- **Coupling/cohesion**: ✓ Domain and application layers depend only on SLF4J interface; implementation swappable
- **Clear module boundaries and contracts**: ✓ Structured JSON log schema consistent across all modules
- **Testability and observability**: ✓ SLF4J easily mocked; MDC enables request-level correlation
- **Security-by-design**: ✓ Sensitive data exclusion enforced by convention; audit trail separated
- **Maintainability and evolvability**: ✓ SLF4J facade makes logging implementation swappable with zero application code changes
