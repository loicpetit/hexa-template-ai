package hexa.template.ai.email.domain.entities;

import hexa.template.ai.email.domain.exceptions.InvalidAuditFieldException;
import hexa.template.ai.email.domain.exceptions.InvalidEmailValueException;

import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Email domain entity per TREQ-0003.
 *
 * Modelled as an immutable Java record; business invariants are enforced in the
 * compact constructor so that no invalid instance can ever exist.
 *
 * Immutable components : id, created, createdBy
 * Mutable components   : value, updated, updatedBy — changes return a NEW record
 *                        (update-by-replacement, per TREQ-0003 semantic versioning)
 *
 * No framework dependencies — pure Java domain logic.
 */
public record Email(
        String id,
        String value,
        Instant created,
        Instant updated,
        String createdBy,
        String updatedBy
) {
    // RFC 5322 basic email pattern (localpart@domain.extension)
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** Compact constructor — validates every construction path. */
    public Email {
        // id can be null (will be assigned by persistence layer)
        Objects.requireNonNull(created, "created cannot be null");
        Objects.requireNonNull(updated, "updated cannot be null");
        validateEmailValue(value);
        validateAuditField(createdBy, "createdBy");
        validateAuditField(updatedBy, "updatedBy");
    }

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    /**
     * Create a brand-new email entity.
     * Sets both created/updated timestamps to now. Id will be assigned by the persistence layer.
     * createdBy and updatedBy are initialised to the same actor (no update history yet).
     *
     * @param value     email address (validated)
     * @param createdBy authenticated user id (non-blank)
     * @return a valid Email (id will be null until persisted)
     */
    public static Email create(String value, String createdBy) {
        Instant now = Instant.now();
        return new Email(
                null,  // id assigned by persistence layer
                value,
                now,
                now,
                createdBy,
                createdBy
        );
    }

    // -------------------------------------------------------------------------
    // Domain behaviour
    // -------------------------------------------------------------------------

    /**
     * Return a new record with the updated email value, updatedBy and a fresh updated timestamp.
     * The original record is unchanged (records are immutable by nature).
     *
     * @param newValue  new email address (validated)
     * @param updatedBy authenticated user id who performed the update (non-blank)
     * @return new Email instance representing the updated state
     */
    public Email update(String newValue, String updatedBy) {
        return new Email(
                this.id,
                newValue,
                this.created,
                Instant.now(),
                this.createdBy,
                updatedBy
        );
    }

    // -------------------------------------------------------------------------
    // Equality — identity-based (id is the business key)
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // -------------------------------------------------------------------------
    // Validation helpers
    // -------------------------------------------------------------------------

    private static void validateEmailValue(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidEmailValueException("Email value cannot be null or blank");
        }
        if (!EMAIL_PATTERN.matcher(value.trim()).matches()) {
            throw new InvalidEmailValueException(
                    "Invalid email format: '" + value + "'. Expected pattern: localpart@domain.extension");
        }
    }

    private static void validateAuditField(String fieldValue, String fieldName) {
        if (fieldValue == null || fieldValue.isBlank()) {
            throw new InvalidAuditFieldException(fieldName + " cannot be null or blank");
        }
    }
}
