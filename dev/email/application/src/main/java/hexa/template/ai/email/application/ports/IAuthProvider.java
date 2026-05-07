package hexa.template.ai.email.application.ports;

import hexa.template.ai.email.application.dto.User;

/**
 * Outbound port interface for user authentication and identity resolution.
 *
 * Abstracts authentication details from use cases and domain logic. Allows swapping
 * between JWT, OAuth2, API key, or other authentication schemes without changing
 * business logic.
 *
 * Per TREQ-0002: All email record operations require authentication. User identity
 * resolved by this port is used for authorization checks and audit attribution.
 * Unauthenticated requests result in UnauthorizedException (HTTP 401).
 *
 * Implementations: JwtAuthProvider, OAuth2Provider, ApiKeyAuthProvider
 */
public interface IAuthProvider {

    /**
     * Resolves the current authenticated user from the request context.
     *
     * Extracts authentication credentials (token, session, etc.) from the HTTP request
     * context, validates them, and returns the resolved user identity if valid.
     *
     * Returns null if the request is unauthenticated or credentials are invalid.
     * Use cases must check for null and throw UnauthorizedException if needed.
     *
     * @return the authenticated User if valid credentials are present, null if unauthenticated
     */
    User getCurrentUser();
}
