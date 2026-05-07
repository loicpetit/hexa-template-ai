package hexa.template.ai.email.application.exceptions;

/**
 * Exception thrown when a requested entity cannot be found in the repository.
 *
 * Per TREQ-0006: Repository delete and read operations throw this when entity is absent.
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
