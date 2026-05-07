# TREQ-0007 - Technology Stack: Language, Runtime & Web Framework

## Metadata
- ID: TREQ-0007
- Status: Approved
- Created: 2026-05-07
- Updated: 2026-05-07
- Author Agent: Software Architect
- Source User Stories: US-0001, US-0002, US-0003, US-0004, US-0005
- Related IDs: REQ-0001, TREQ-0001, TREQ-0004
- Source Links: [docs/architecture/architecture-overview.md](../docs/architecture/architecture-overview.md)
- Architecture Links: [docs/architecture/architecture-overview.md](../docs/architecture/architecture-overview.md)

## Technical Requirement Statement
The system shall be implemented using a typed programming language with a robust web framework suitable for HTTP REST API development. The technology stack shall support the hexagonal architecture pattern (clear separation of domain, application, adapters, and ports layers) and shall provide mature ecosystems for data persistence, authentication, logging, and testing. The stack must balance developer productivity with production-readiness and operational simplicity.

## Constraints
- Language must support strong typing (static type checking) to catch errors at development time.
- Web framework must support HTTP REST API development (not limited to templating/server-side rendering).
- Stack must have mature, well-documented libraries for ORM/query building, logging, and testing.
- Must have active community and regular security updates.
- Must support the hexagonal architecture layer model (no framework forced into domain layer).
- Deployment must be straightforward (single binary, container-ready, or simple install).

## Existing Coverage Check
- Similar TREQ checked: None (technology stack not yet specified)
- Already covered: None; this is foundational choice
- Gap: Architecture defined (TREQ-0001) but no language/framework selected

## Current Architecture Baseline
- Existing stack and patterns considered: Hexagonal architecture requires framework-independent domain layer
- Related approved TREQs: TREQ-0001 (architecture), TREQ-0002 (API Key auth), TREQ-0004 (REST API endpoints)
- Consistency expectation for this decision: Stack must enable clean separation between layers

## Alternatives

### Option A: Node.js + TypeScript + Express.js
**Technology Profile**:
- **Runtime**: Node.js (JavaScript runtime)
- **Language**: TypeScript (typed superset of JavaScript)
- **Framework**: Express.js (minimal HTTP framework)
- **Typing**: Static types via TypeScript compiler

**Pros**:
- Fast development iteration (JavaScript flexibility + TypeScript safety)
- Excellent tooling ecosystem (npm, vast package availability)
- REST API focus (Express.js is lightweight, not opinionated)
- Strong community (widespread adoption, many tutorials/examples)
- Supports hexagonal architecture (no forced patterns)
- Runs on Windows/Mac/Linux; easy deployment
- Good performance for I/O-bound workloads (Node.js strength)
- TypeScript catches type errors at compile time

**Cons**:
- Single-threaded event loop (CPU-bound tasks require worker threads)
- Memory usage can be higher than compiled languages
- Newer ecosystem (less battle-tested than Java for enterprise)

**Consistency impact**: Aligned with modern API development practices; good for CRUD/I/O-heavy services

---

### Option B: Python + FastAPI
**Technology Profile**:
- **Runtime**: Python (CPython or PyPy)
- **Language**: Python (dynamic, readable)
- **Framework**: FastAPI (modern async framework)
- **Typing**: Optional static types via type hints

**Pros**:
- Highly readable code (Python's strength)
- FastAPI is modern and performant (async/await support)
- Fast prototyping (less boilerplate than typed systems)
- Good ORM ecosystem (SQLAlchemy, Pydantic)
- Strong community for data science/ML (if future expansion)
- Good support for hexagonal architecture

**Cons**:
- Dynamic typing (type errors discovered at runtime, not compile time)
- Slower execution than Node.js or compiled languages
- Deployment (Python interpreter must be installed; larger Docker images)
- FastAPI is newer than Express.js (less battle-tested in production)

**Consistency impact**: Good for rapid prototyping; less suitable for large teams (dynamic typing risk)

---

### Option C: Java + Spring Boot
**Technology Profile**:
- **Runtime**: Java Virtual Machine (JVM)
- **Language**: Java or Kotlin (statically typed)
- **Framework**: Spring Boot (comprehensive framework ecosystem)
- **Typing**: Static, enforced at compile time

**Pros**:
- Enterprise-grade maturity (battle-tested for 20+ years)
- Excellent performance (JVM optimization, concurrency support)
- Strong static typing (compile-time error detection)
- Vast ecosystem (mature libraries for everything)
- Excellent support for large teams (strong tooling, clear patterns)
- Good support for hexagonal architecture (Domain-Driven Design roots)

**Cons**:
- Verbose (boilerplate code; slower initial development)
- Steeper learning curve (requires understanding JVM concepts)
- Slower startup time (JVM warmup required)
- Heavier deployment (JVM + application size)
- Overkill for small POC

**Consistency impact**: Excellent for large-scale systems; overkill for POC

---

## Top 3 Ranking

1. **Option C: Java + Spring Boot** *(Selected)*
   - Why: Enterprise-grade maturity, strong static typing, and first-class hexagonal/DDD architecture support. Excellent long-term maintainability and team scalability.

2. **Option A: Node.js + TypeScript + Express.js**
   - Why: Fast iteration for POC; lighter weight. Consider if startup speed or solo development is prioritised.

3. **Option B: Python + FastAPI**
   - Why: Good for rapid prototyping; less suitable for larger teams or long-lived systems.

## Trade-Offs

- **Verbosity vs. Maturity**: Java requires more boilerplate than TypeScript or Python, but provides battle-tested patterns and strong compile-time guarantees. Trade accepted: long-term maintainability over initial speed.

- **Startup time vs. Runtime performance**: JVM has a slower cold start but excellent sustained throughput. Trade accepted: acceptable for a non-serverless deployment.

- **Comprehensiveness vs. Minimalism**: Spring Boot brings a full ecosystem upfront; more to configure initially but less library hunting over time. Trade accepted.

## Impact Analysis

- **Functional impact**: None — technology doesn't change what the API does, only how it's built

- **Module impact**: 
  - Project structure must match hexagonal layers: domain/, application/, adapters/, ports/
  - Framework selection impacts build and deployment tooling

- **Interface and contract impact**: 
  - REST API contract (TREQ-0004) framework-independent (HTTP is platform-neutral)
  - Internal module contracts (ports) must be implemented in chosen language

- **Backward compatibility**: N/A (new project); future migration possible if architecture adhered to (hexagonal allows tech swaps)

- **Data and migration impact**: N/A (new project)

- **Operational impact**:
  - Deployment: Packaged as executable JAR; easy to containerize (Docker)
  - Scaling: Horizontal scaling straightforward (stateless HTTP); JVM handles high concurrency well
  - Monitoring: Spring Boot Actuator provides health, metrics, and info endpoints out of the box
  - Maintenance: Regular JDK and Spring Boot updates for security patches

- **Security and compliance impact**:
  - Java ecosystem has mature security tooling (OWASP Dependency-Check, Snyk, etc.)
  - Spring Security available for future auth extensions
  - HTTPS/TLS enforcement at deployment layer

- **Testing impact**:
  - Testing frameworks: JUnit 5 (unit), Mockito (mocking), Spring Boot Test / MockMvc (HTTP integration)
  - Strong static typing and IDE support (IntelliJ, Eclipse) aid test generation

- **Traceability impact**:
  - Technology stack decision linked to TREQ-0001 (architecture), TREQ-0004 (API), TREQ-0006 (persistence)

## Recommendation
- **Proposed best option**: Option C — Java + Spring Boot *(requester selected)*
- **Why**:
  - Enterprise-grade maturity and battle-tested in production environments
  - Strong static typing enforced at compile time
  - Excellent hexagonal and DDD architecture support
  - Comprehensive ecosystem (Spring Web, Spring Security, Actuator, testing)
  - Wide community, extensive documentation, long-term support
- **Final decision owner**: Requester

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|-----------|
| JVM slow cold start in containerised environments | Low | Low | Use JVM warm-up or GraalVM native image if startup time becomes critical |
| Spring Boot version upgrades introduce breaking changes | Low | Medium | Pin Spring Boot version; follow migration guides on each upgrade |
| Verbose boilerplate slows initial development | Medium | Low | Use Lombok or Kotlin to reduce boilerplate; leverage Spring Boot auto-configuration |
| Team unfamiliar with Spring Boot | Low | Medium | Official Spring guides and documentation are comprehensive; ramp-up typically 1-2 weeks |

## Consistency Exception Assessment
- Exception needed: No
- Java + Spring Boot natively supports hexagonal architecture (language and framework do not constrain layer separation)

## Validation
- Requester validation required: Yes
- Validation status: Approved
- Requester selected option: C — Java + Spring Boot

## Notes
- **Version guidance**: Use Java 25; Spring Boot 4.x (latest stable)
- **Build tooling**: Gradle with Groovy DSL
- **Project bootstrap**: Spring Initializr (start.spring.io) for project scaffolding
- **Future flexibility**: Architecture (TREQ-0001) ensures domain and application layers are framework-independent. Spring Boot is confined to the adapter layer; it can be replaced without touching domain or use cases.

## Architecture Best Practices Compliance

- **Separation of concerns**: ✓ Spring Boot is confined to adapters; domain and application layers are plain Java (no framework dependency)
- **Coupling/cohesion**: ✓ Java interfaces and dependency injection naturally enforce port/adapter contracts
- **Clear module boundaries and contracts**: ✓ Java interfaces express ports explicitly; Spring DI wires adapters without leaking into domain
- **Testability and observability**: ✓ JUnit 5 + Mockito for unit tests; Spring Boot Test / MockMvc for integration tests; Actuator for runtime observability
- **Security-by-design**: ✓ Compile-time type safety; Spring Security available; mature Java security tooling
- **Maintainability and evolvability**: ✓ Strong typing, comprehensive IDE support, and well-understood enterprise patterns support long-term maintenance
