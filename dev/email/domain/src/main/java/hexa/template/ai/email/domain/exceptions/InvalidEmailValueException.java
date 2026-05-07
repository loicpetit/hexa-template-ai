package hexa.template.ai.email.domain.exceptions;

public class InvalidEmailValueException extends RuntimeException {

    public InvalidEmailValueException(String message) {
        super(message);
    }

    public InvalidEmailValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
