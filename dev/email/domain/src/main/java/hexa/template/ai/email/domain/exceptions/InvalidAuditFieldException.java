package hexa.template.ai.email.domain.exceptions;

public class InvalidAuditFieldException extends RuntimeException {

    public InvalidAuditFieldException(String message) {
        super(message);
    }

    public InvalidAuditFieldException(String message, Throwable cause) {
        super(message, cause);
    }
}
