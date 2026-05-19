package hexa.template.ai.email.adapter.rest;

import hexa.template.ai.email.application.exceptions.DuplicateKeyException;
import hexa.template.ai.email.application.exceptions.UnauthorizedException;
import hexa.template.ai.email.domain.exceptions.InvalidAuditFieldException;
import hexa.template.ai.email.domain.exceptions.InvalidEmailValueException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateKeyException ex) {
        return buildError(HttpStatus.CONFLICT, "DUPLICATE_KEY", ex.getMessage());
    }

    @ExceptionHandler({InvalidEmailValueException.class, InvalidAuditFieldException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleInvalidInput(RuntimeException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "INVALID_INPUT", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred");
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String code, String message) {
        ErrorResponse error = new ErrorResponse(
                status.value(),
                message,
                code,
                DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC).format(Instant.now())
        );
        return ResponseEntity.status(status).body(error);
    }

    public record ErrorResponse(int status, String message, String code, String timestamp) {}
}
