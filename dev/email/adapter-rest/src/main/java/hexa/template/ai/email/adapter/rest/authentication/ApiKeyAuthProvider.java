package hexa.template.ai.email.adapter.rest.authentication;

import hexa.template.ai.email.application.dto.User;
import hexa.template.ai.email.application.ports.IAuthProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

/**
 * REST adapter implementation of IAuthProvider for API Key authentication.
 *
 * Extracts API key from the Authorization header of the HTTP request and validates it
 * against a provided key-to-user mapping. Supports both "Bearer <key>" and "ApiKey <key>"
 * header formats.
 *
 * Per TREQ-0002: API Key is the selected authentication mechanism for POC.
 *
 * Usage: Instantiate in REST controller with request and key map, then inject into use cases.
 * Request-scoped: each HTTP request gets a fresh provider instance.
 */
public class ApiKeyAuthProvider implements IAuthProvider {

    private final Map<String, User> apiKeyToUserMap;
    private final HttpServletRequest request;

    /**
     * Constructs an API Key authentication provider.
     *
     * @param apiKeyToUserMap mapping of valid API keys to User objects (non-null, may be empty)
     * @param request the current HTTP request (may be null for testing)
     */
    public ApiKeyAuthProvider(Map<String, User> apiKeyToUserMap, HttpServletRequest request) {
        if (apiKeyToUserMap == null) {
            throw new IllegalArgumentException("API key to user map must not be null");
        }
        this.apiKeyToUserMap = apiKeyToUserMap;
        this.request = request;
    }

    /**
     * Resolves the current authenticated user from the Authorization header.
     *
     * Extracts the API key from the Authorization header in either "Bearer <key>" or
     * "ApiKey <key>" format. Returns the corresponding User if the key is valid,
     * or null if the header is missing, malformed, or the key is not recognized.
     *
     * @return an Optional containing the authenticated User if a valid API key is present,
     *         or an empty Optional if unauthenticated
     */
    @Override
    public Optional<User> getCurrentUser() {
        if (request == null) {
            return Optional.empty();
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            return Optional.empty();
        }

        // Extract API key from supported header formats
        String apiKey = extractApiKey(authHeader);
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        // Look up user by API key
        return Optional.ofNullable(apiKeyToUserMap.get(apiKey));
    }

    /**
     * Extracts API key from Authorization header.
     *
     * Supports:
     * - "ApiKey <key>" format
     *
     * @param authHeader the Authorization header value
     * @return the API key, or null if the header format is not recognized
     */
    private String extractApiKey(String authHeader) {
        if (authHeader.startsWith("ApiKey ")) {
            return authHeader.substring("ApiKey ".length());
        }
        return null;
    }
}
