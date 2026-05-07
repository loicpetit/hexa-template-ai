package hexa.template.ai.email.application.dto;

import java.util.List;

/**
 * User POJO representing an authenticated user identity.
 * Returned by IAuthProvider.getCurrentUser() and passed to use cases for audit attribution.
 *
 * Per TREQ-0002: User represents the resolved authenticated identity.
 */
public record User(String name, List<String> roles) {

    public User {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("User name must not be null or blank");
        }
        if (roles == null) {
            throw new IllegalArgumentException("User roles must not be null");
        }
    }

    /**
     * Convenience constructor with empty roles.
     */
    public User(String name) {
        this(name, List.of());
    }
}

