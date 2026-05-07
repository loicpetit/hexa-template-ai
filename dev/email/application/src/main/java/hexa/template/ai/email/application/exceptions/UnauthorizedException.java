package hexa.template.ai.email.application.exceptions;

/**
 * Exception thrown when a user attempts to perform an operation without proper authentication
 * or authorization.
 *
 * Per TREQ-0002: All email record operations require authentication. Unauthenticated requests
 * result in this exception, which maps to HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
