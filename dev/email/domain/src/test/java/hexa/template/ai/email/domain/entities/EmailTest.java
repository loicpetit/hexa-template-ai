package hexa.template.ai.email.domain.entities;

import hexa.template.ai.email.domain.exceptions.InvalidAuditFieldException;
import hexa.template.ai.email.domain.exceptions.InvalidEmailValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Email domain entity (TREQ-0003 / US-0001).
 */
@DisplayName("Email Domain Entity")
class EmailTest {

    private static final String VALID_EMAIL   = "test@example.com";
    private static final String VALID_USER_ID = "user-123";

    // ------------------------------------------------------------------
    // Creation — happy path
    // ------------------------------------------------------------------

    @Test
    @DisplayName("create() returns a valid record with all fields set correctly")
    void shouldCreateValidEmailRecord() {
        Email email = Email.create(VALID_EMAIL, VALID_USER_ID);

        assertNotNull(email.id());
        assertEquals(VALID_EMAIL, email.value());
        assertNotNull(email.created());
        assertNotNull(email.updated());
        assertEquals(VALID_USER_ID, email.createdBy());
        assertEquals(VALID_USER_ID, email.updatedBy());
        // On a new record created == updated and createdBy == updatedBy
        assertEquals(email.created(), email.updated());
        assertEquals(email.createdBy(), email.updatedBy());
    }

    @Test
    @DisplayName("create() generates a different UUID for each record")
    void shouldGenerateUniqueIds() {
        Email r1 = Email.create("a@example.com", VALID_USER_ID);
        Email r2 = Email.create("b@example.com", VALID_USER_ID);

        assertNotEquals(r1.id(), r2.id());
    }

    // ------------------------------------------------------------------
    // Email value validation
    // ------------------------------------------------------------------

    @Test
    @DisplayName("create() rejects null email value")
    void shouldRejectNullEmailValue() {
        assertThrows(InvalidEmailValueException.class,
                () -> Email.create(null, VALID_USER_ID));
    }

    @Test
    @DisplayName("create() rejects empty email value")
    void shouldRejectEmptyEmailValue() {
        assertThrows(InvalidEmailValueException.class,
                () -> Email.create("", VALID_USER_ID));
    }

    @Test
    @DisplayName("create() rejects blank (whitespace-only) email value")
    void shouldRejectBlankEmailValue() {
        assertThrows(InvalidEmailValueException.class,
                () -> Email.create("   ", VALID_USER_ID));
    }

    @Test
    @DisplayName("create() rejects email without @")
    void shouldRejectEmailMissingAt() {
        assertThrows(InvalidEmailValueException.class,
                () -> Email.create("notanemail.com", VALID_USER_ID));
    }

    @Test
    @DisplayName("create() rejects email without local part")
    void shouldRejectEmailMissingLocalPart() {
        assertThrows(InvalidEmailValueException.class,
                () -> Email.create("@example.com", VALID_USER_ID));
    }

    @Test
    @DisplayName("create() rejects email without domain extension")
    void shouldRejectEmailMissingExtension() {
        assertThrows(InvalidEmailValueException.class,
                () -> Email.create("user@example", VALID_USER_ID));
    }

    @Test
    @DisplayName("create() accepts email with +, ., _, - in local part and subdomains")
    void shouldAcceptVariousValidEmailFormats() {
        assertDoesNotThrow(() -> Email.create("user+tag@example.co.uk", VALID_USER_ID));
        assertDoesNotThrow(() -> Email.create("first.last@subdomain.example.com", VALID_USER_ID));
        assertDoesNotThrow(() -> Email.create("user_name@example-domain.com", VALID_USER_ID));
    }

    // ------------------------------------------------------------------
    // Audit field validation
    // ------------------------------------------------------------------

    @Test
    @DisplayName("create() rejects null createdBy")
    void shouldRejectNullCreatedBy() {
        assertThrows(InvalidAuditFieldException.class,
                () -> Email.create(VALID_EMAIL, null));
    }

    @Test
    @DisplayName("create() rejects empty createdBy")
    void shouldRejectEmptyCreatedBy() {
        assertThrows(InvalidAuditFieldException.class,
                () -> Email.create(VALID_EMAIL, ""));
    }

    @Test
    @DisplayName("create() rejects blank createdBy")
    void shouldRejectBlankCreatedBy() {
        assertThrows(InvalidAuditFieldException.class,
                () -> Email.create(VALID_EMAIL, "   "));
    }

    // ------------------------------------------------------------------
    // Update — returns new record; original immutable
    // ------------------------------------------------------------------

    @Test
    @DisplayName("update() returns a new record with new value and updatedBy")
    void shouldReturnNewRecordOnUpdate() throws InterruptedException {
        Email original = Email.create(VALID_EMAIL, VALID_USER_ID);
        Thread.sleep(5);

        Email updated = original.update("new@example.com", "user-456");

        assertEquals("new@example.com", updated.value());
        assertEquals("user-456", updated.updatedBy());
        assertTrue(updated.updated().isAfter(updated.created()),
                "updated timestamp must be after created timestamp");
        // Identity and immutable fields preserved
        assertEquals(original.id(), updated.id());
        assertEquals(original.created(), updated.created());
        assertEquals(original.createdBy(), updated.createdBy());
    }

    @Test
    @DisplayName("update() does not mutate the original record")
    void shouldNotMutateOriginalRecord() throws InterruptedException {
        Email original = Email.create(VALID_EMAIL, VALID_USER_ID);
        Thread.sleep(5);

        original.update("new@example.com", "user-456");

        assertEquals(VALID_EMAIL, original.value());
        assertEquals(VALID_USER_ID, original.updatedBy());
    }

    @Test
    @DisplayName("update() rejects invalid email")
    void shouldRejectInvalidEmailInUpdate() {
        Email email = Email.create(VALID_EMAIL, VALID_USER_ID);
        assertThrows(InvalidEmailValueException.class,
                () -> email.update("not-an-email", "user-456"));
    }

    @Test
    @DisplayName("update() rejects null updatedBy")
    void shouldRejectNullUpdatedBy() {
        Email email = Email.create(VALID_EMAIL, VALID_USER_ID);
        assertThrows(InvalidAuditFieldException.class,
                () -> email.update("new@example.com", null));
    }

    @Test
    @DisplayName("update() rejects blank updatedBy")
    void shouldRejectBlankUpdatedBy() {
        Email email = Email.create(VALID_EMAIL, VALID_USER_ID);
        assertThrows(InvalidAuditFieldException.class,
                () -> email.update("new@example.com", ""));
    }

    // ------------------------------------------------------------------
    // Identity-based equality
    // ------------------------------------------------------------------

    @Test
    @DisplayName("equality is based on id only, not on other fields")
    void shouldUseIdForEquality() {
        Email r1 = Email.create("a@example.com", VALID_USER_ID);
        Email r2 = Email.create("b@example.com", VALID_USER_ID);

        assertNotEquals(r1, r2);
    }

    @Test
    @DisplayName("toString() contains key field information")
    void shouldProduceInformativeToString() {
        Email email = Email.create(VALID_EMAIL, VALID_USER_ID);
        String s = email.toString();

        assertTrue(s.contains("Email"));
        assertTrue(s.contains(VALID_EMAIL));
    }
}
