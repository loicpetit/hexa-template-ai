# TREQ-0002 - Authentication & Authorization Architecture

## Metadata
- ID: TREQ-0002
- Status: Approved
- Created: 2026-05-07
- Updated: 2026-05-07
- Author Agent: Software Architect
- Source User Stories: US-0001, US-0002, US-0003, US-0004, US-0005
- Related IDs: REQ-0002, TREQ-0001
- Source Links: [requirements/REQ-0002-authenticated-email-access-and-audit.md](../requirements/REQ-0002-authenticated-email-access-and-audit.md), [docs/architecture/architecture-overview.md](../docs/architecture/architecture-overview.md)
- Architecture Links: [docs/architecture/architecture-overview.md](../docs/architecture/architecture-overview.md)

## Technical Requirement Statement
The system shall enforce authentication (identity verification) on all email record operations (create, read, update, delete) and shall provide a pluggable authentication mechanism through an authentication port interface. Each authenticated request shall include a unique user identity (id, name, email) that is available to use cases for authorization checks and audit attribution. Unauthenticated or unauthorized requests shall be rejected with a 401 Unauthorized response.

## Constraints
- Authentication must be verified before any use case logic executes.
- All email record operations (CRUD) must require authentication; no anonymous access.
- User identity must be available to use cases for audit attribution (createdBy, updatedBy).
- Authentication implementation must be pluggable via the IAuthProvider port interface.
- Authentication errors (invalid token, expired session) must not expose sensitive details in response.

## Existing Coverage Check
- Similar TREQ checked: None (first auth TREQ)
- Already covered: Partially by REQ-0002 (business requirement); technical how to implement is not specified
- Gap: REQ-0002 specifies what (all operations require auth) but not how (mechanism, token format, scheme)

## Current Architecture Baseline
- Existing stack and patterns considered: Hexagonal architecture (TREQ-0001) defines auth as outbound port
- Related approved TREQs: TREQ-0001 (architecture layer defines AuthProvider port)
- Consistency expectation for this decision: Authentication adapter must implement IAuthProvider port; all use cases must check authenticated user via this port

## Technical Module Organization

### Authentication Port (Contract)
**Location**: `ports/authentication-port.ts`
**Responsibility**: Abstract authentication details from use cases and domain  
**Interface**:
```typescript
interface IAuthProvider {
  /**
   * Resolve current user from request context
   * Returns User object with verified identity or null if unauthenticated
   */
  getCurrentUser(request: HttpRequest): Promise<User | null>
}

interface User {
  id: string                  // Unique user identifier (from identity provider)
  name: string               // User display name
  email: string              // User email address
  roles?: string[]           // Optional: user roles for future authorization rules
}
```

### Authentication Enforcement (Application Layer)
**Location**: `application/use-cases/*`  
**Responsibility**: Check authenticated user before executing use case logic  
**Pattern** (each use case):
```typescript
async execute(request: CreateEmailRecordRequest) {
  // 1. Authenticate: check user identity
  const user = await this.authProvider.getCurrentUser(request.httpContext)
  if (!user) {
    throw new UnauthorizedError('Authentication required')
  }
  
  // 2. Authorize: check permissions (future use)
  // if (!this.canUserCreateEmail(user)) {
  //   throw new ForbiddenError('Permission denied')
  // }
  
  // 3. Execute business logic with authenticated user context
  const email = await this.createEmailRecord(user, request.emailValue)
  return email
}
```

### Authentication Adapter (Implementation)
**Location**: `adapters/driven/authentication-adapter`  
**Responsibility**: Implement IAuthProvider; verify tokens and retrieve user identity  
**Subcomponents** (selected per option below):
- Token parser: Extract token from HTTP headers
- Token validator: Verify signature and expiration
- User resolver: Look up user details (from local store, external directory, etc.)
- Error mapper: Map authentication errors to application exceptions

---

## Alternatives

### Option A: Stateless JWT (JSON Web Tokens)
**How it works**:
- Client obtains JWT token from identity provider (outside scope of this system)
- Client includes token in every request: `Authorization: Bearer <JWT>`
- Authentication adapter verifies JWT signature, checks expiration, decodes claims
- User identity extracted from JWT claims (id, name, email)

**Pros**:
- Stateless: no server-side session storage needed
- Scalable: works with multiple server instances (no session affinity)
- Standard: JWT is widely used, well-documented, many libraries available
- Decoupling: identity provider can be separate service (Auth0, AWS Cognito, etc.)

**Cons**:
- Token revocation: can't immediately invalidate compromised tokens (must wait for expiration)
- Payload size: JWT adds overhead to every request
- Complexity: requires understanding of token claims, signature verification

**Consistency impact**: Aligned with modern REST API practices; plays well with distributed systems

---

### Option B: OAuth2 with Bearer Tokens
**How it works**:
- Client redirects user to OAuth2 authorization server
- User grants permission; OAuth2 server returns authorization code
- Client exchanges code for access token (and optionally refresh token)
- Client includes access token in requests: `Authorization: Bearer <access_token>`
- Authentication adapter validates token with OAuth2 server (introspection endpoint)
- User identity retrieved from userinfo endpoint or token claims

**Pros**:
- Standardized: OAuth2 is industry standard for delegation (users don't share passwords)
- Flexible: supports multiple grant types (authorization code, client credentials, etc.)
- Token revocation: tokens can be revoked immediately via introspection endpoint
- Third-party integration: easily integrate with existing OAuth2 providers (Google, GitHub, etc.)

**Cons**:
- Complexity: more moving parts (auth server, token endpoint, userinfo endpoint)
- Latency: validation requires call to auth server for each request (unless caching)
- Coupling: depends on external OAuth2 provider availability
- Setup overhead: requires auth server configuration

**Consistency impact**: Industry standard; recommended for multi-tenant or public APIs

---

### Option C: API Key
**How it works**:
- Client is issued a static API key (long-lived secret)
- Client includes key in request header: `X-API-Key: <api_key>`
- Authentication adapter looks up key in local database or cache
- If valid, retrieve associated user identity and permissions

**Pros**:
- Simple: minimal implementation complexity; straightforward to test
- No external dependency: no need for external auth provider
- Suitable for internal/service-to-service APIs

**Cons**:
- Poor security: secrets stored on client side (risk of exposure)
- Static: difficult to rotate or revoke individual keys
- Not suitable for user-facing APIs: users would see raw keys
- Scaling: requires key lookup in database for each request

**Consistency impact**: Too simple for user-facing APIs; acceptable only for internal service APIs

---

## Top 3 Ranking

1. **Option A: Stateless JWT**
   - Why: Best balance for REST APIs. Stateless, scalable, standard. Works with multiple server instances. Widely used and well-supported. Aligns with modern API architecture.

2. **Option B: OAuth2 with Bearer Tokens**
   - Why: Better for multi-tenant systems and third-party integration. More secure token revocation. Recommended if integration with existing identity providers (Google, GitHub, corporate LDAP) is planned.

3. Option C: API Key
   - Why: Not recommended for user-facing APIs. Too simple for production systems. Acceptable only if this is purely internal service-to-service API.

## Trade-Offs

- **Statefulness vs. Scalability**: JWT is stateless but can't be revoked immediately. OAuth2 is stateful (requires server call) but offers revocation. Trade: JWT chosen for simplicity unless revocation requirement emerges.

- **Simplicity vs. Security**: API key is simplest but weakest. JWT/OAuth2 require more setup but production-ready. Trade: JWT chosen for good security-to-complexity ratio.

- **Local auth vs. Delegated auth**: JWT often used with external identity provider (simplicity of validation). OAuth2 requires external provider (standard delegation). Trade: Either is acceptable depending on whether auth provider is internal or external.

## Impact Analysis

- **Functional impact**: 
  - All CRUD operations now require authenticated requests
  - Unauthenticated requests rejected with 401
  - User identity available to use cases for audit attribution

- **Module impact**:
  - Adds: Authentication port (interface)
  - Adds: Authentication adapter (implementation)
  - Modifies: All use cases (add authentication check at start)
  - Modifies: Primary adapter (extract auth context from request header)

- **Interface and contract impact**:
  - HTTP contract: All endpoints now require `Authorization: Bearer <token>` header (if JWT selected)
  - Internal contract: Use cases receive User object from auth port
  - Error responses: 401 Unauthorized for missing/invalid token

- **Backward compatibility**: N/A (new project)

- **Data and migration impact**: 
  - If Option B (OAuth2) selected: user identity stored in external provider; no local data migration
  - If Option A (JWT) selected: user identity decoded from token claims; no local storage needed
  - If Option C (API Key) selected: keys stored in local database or cache; key rotation required periodically

- **Operational impact**:
  - JWT: No operational overhead; tokens are self-verifying
  - OAuth2: Requires monitoring auth server availability; cache tokens to reduce latency
  - API Key: Requires key management (issuance, rotation, revocation); database queries per request

- **Security and compliance impact**:
  - Authentication enforced on all operations (no anonymous access)
  - User identity traced for audit (createdBy/updatedBy filled with authenticated user id)
  - Confidentiality: Tokens sent over HTTPS (enforced at deployment layer, not this TREQ)
  - Integrity: Token signature verified (if cryptographic token) or validated with server
  - Compliance: Audit trail captures who performed each action (supports regulatory requirements)

- **Testing impact**:
  - Unit tests: Mock auth provider injects test user; tests don't call real auth service
  - Integration tests: Use test token or test API key; auth provider returns mock user
  - E2E tests: Use valid token or key from test auth provider; verify 401 response for invalid token
  - Scope: Authentication code path tested separately from business logic

- **Traceability impact**:
  - REQ-0002 (business requirement: all operations require auth) → TREQ-0002 (technical mechanism)
  - US-0001 through US-0004 all include auth check
  - E2E tests verify 401 for unauthenticated requests

## Recommendation
- **Proposed best option**: Option A — Stateless JWT
- **Why**: 
  - Stateless design aligns with REST API best practices
  - Scalable (no session affinity needed)
  - Industry standard for REST APIs
  - Simple to validate (verify signature, check expiration)
  - Decouples from auth provider (identity provider can be swapped)
  - Widely supported libraries (jsonwebtoken, passport.js, etc.)
- **Final decision owner**: Requester
- **Requester's Decision**: **Option C — API Key** (approved for POC, 2026-05-07)
  - **Rationale**: Simplest authentication mechanism; no external dependencies; suitable for proof of concept
  - **Migration path**: Can be upgraded to JWT/OAuth2 in future without changing port interface

**Implementation notes if JWT selected**:
- Token format: `Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
- Verification: Check signature with public key (if asymmetric) or secret (if symmetric)
- Claims: Extract `sub` (user id), `name`, `email` from decoded payload
- Expiration: Reject tokens with `exp` claim in the past
- Error handling: Invalid signature → 401, Expired token → 401, Missing token → 401

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|-----------|
| Compromised JWT token remains valid until expiration | Medium | High | Implement short expiration times (15-30 min); use refresh tokens for long-lived sessions (Option A+) |
| Auth service outage prevents system access (OAuth2 Option B) | Medium | High | Cache token validation; implement fallback (local key storage); monitor auth service health |
| Tokens exposed in logs or error messages | Low | High | Sanitize logs; don't log full token; use token fingerprint for debugging |
| User permissions change but token remains valid | Low | Medium | Include permission claims in token; refresh token on permission change; add token revocation list if needed |

## Consistency Exception Assessment
- Exception needed: No
- JWT aligns with REST API best practices and hexagonal architecture (port interface allows swapping implementations)

## Validation
- Requester validation required: Yes
- Validation status: Approved ✓ (2026-05-07)
- Requester selected option: Option C — API Key (POC phase, simple authentication without external dependencies)

## Notes
- Authentication is separate from authorization. This TREQ covers identity verification (auth). Authorization (checking permissions/roles) can be added later in a separate TREQ if needed.
- Token transport security (HTTPS) is a deployment concern, not covered by this TREQ. All tokens must be sent over HTTPS in production.
- User identity resolution (getting user name, email from token claims) depends on the auth provider configuration. Adapter must handle multiple claim formats.

## Architecture Best Practices Compliance

- **Separation of concerns**: ✓ Authentication isolated in port + adapter; use cases don't know implementation details
- **Coupling/cohesion**: ✓ Low coupling (IAuthProvider interface); high cohesion (auth adapter focused on token verification)
- **Clear module boundaries and contracts**: ✓ Port interface clearly defines what auth adapter must provide; use cases depend only on interface
- **Testability and observability**: ✓ Mock auth provider easy to create for tests; authentication failures logged separately
- **Security-by-design**: ✓ Authentication required before any operation; user identity traced for audit; tokens expire; signatures verified
- **Maintainability and evolvability**: ✓ Swapping auth scheme (JWT → OAuth2) requires only adapter change; no use case changes
