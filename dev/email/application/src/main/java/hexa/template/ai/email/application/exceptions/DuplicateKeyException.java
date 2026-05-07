package hexa.template.ai.email.application.exceptions;

/**
 * Exception thrown when an attempt is made to persist an entity with a key (id or unique value)
 * that already exists in the repository.
 *
 * Per TREQ-0006: Repository must enforce uniqueness of both id and email value.
 */
public class DuplicateKeyException extends RuntimeException {

    public DuplicateKeyException(String message) {
        super(message);
    }

    public DuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
