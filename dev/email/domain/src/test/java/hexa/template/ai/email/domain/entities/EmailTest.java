package hexa.template.ai.email.domain.entities;

import hexa.template.ai.email.domain.exceptions.InvalidAuditFieldException;
import hexa.template.ai.email.domain.exceptions.InvalidEmailValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Email domain entity (TREQ-0003 / US-0001).
 * Uses nested test classes for organization and AssertJ for fluent assertions with descriptions.
 */
@DisplayName("Email Domain Entity")
class EmailTest {

    private static final String VALID_EMAIL   = "test@example.com";
    private static final String VALID_USER_ID = "user-123";

    // =========================================================================
    // Creation — Happy Path
    // =========================================================================

    @Nested
    @DisplayName("Creation: Happy Path")
    class Creation {

        @Test
        @DisplayName("create() returns a valid record with all fields set correctly")
        void shouldCreateValidEmailRecord() {
            Email email = Email.create(VALID_EMAIL, VALID_USER_ID);

            assertThat(email.id())
                    .as("id should be generated")
                    .isNotBlank();
            assertThat(email.value())
                    .as("value should match input")
                    .isEqualTo(VALID_EMAIL);
            assertThat(email.created())
                    .as("created timestamp should be set")
                    .isNotNull();
            assertThat(email.updated())
                    .as("updated timestamp should be set")
                    .isNotNull();
            assertThat(email.createdBy())
                    .as("createdBy should match input")
                    .isEqualTo(VALID_USER_ID);
            assertThat(email.updatedBy())
                    .as("updatedBy should equal createdBy on new record")
                    .isEqualTo(VALID_USER_ID);
            assertThat(email.created())
                    .as("created should equal updated on new record")
                    .isEqualTo(email.updated());
        }

        @Test
        @DisplayName("create() generates a different UUID for each record")
        void shouldGenerateUniqueIds() {
            Email r1 = Email.create("a@example.com", VALID_USER_ID);
            Email r2 = Email.create("b@example.com", VALID_USER_ID);

            assertThat(r1.id())
                    .as("each record should have a unique id")
                    .isNotEqualTo(r2.id());
        }
    }

    // =========================================================================
    // Email Value Validation
    // =========================================================================

    @Nested
    @DisplayName("Validation: Email Value")
    class EmailValueValidation {

        @Test
        @DisplayName("create() rejects null email value")
        void shouldRejectNullEmailValue() {
            assertThatThrownBy(() -> Email.create(null, VALID_USER_ID))
                    .as("null email should throw InvalidEmailValueException")
                    .isInstanceOf(InvalidEmailValueException.class)
                    .hasMessageContaining("cannot be null or blank");
        }

        @Test
        @DisplayName("create() rejects empty email value")
        void shouldRejectEmptyEmailValue() {
            assertThatThrownBy(() -> Email.create("", VALID_USER_ID))
                    .as("empty email should throw InvalidEmailValueException")
                    .isInstanceOf(InvalidEmailValueException.class)
                    .hasMessageContaining("cannot be null or blank");
        }

        @Test
        @DisplayName("create() rejects blank (whitespace-only) email value")
        void shouldRejectBlankEmailValue() {
            assertThatThrownBy(() -> Email.create("   ", VALID_USER_ID))
                    .as("blank email should throw InvalidEmailValueException")
                    .isInstanceOf(InvalidEmailValueException.class)
                    .hasMessageContaining("cannot be null or blank");
        }

        @Test
        @DisplayName("create() rejects email without @")
        void shouldRejectEmailMissingAt() {
            assertThatThrownBy(() -> Email.create("notanemail.com", VALID_USER_ID))
                    .as("email without @ should be rejected")
                    .isInstanceOf(InvalidEmailValueException.class)
                    .hasMessageContaining("Invalid email format");
        }

        @Test
        @DisplayName("create() rejects email without local part")
        void shouldRejectEmailMissingLocalPart() {
            assertThatThrownBy(() -> Email.create("@example.com", VALID_USER_ID))
                    .as("email without local part should be rejected")
                    .isInstanceOf(InvalidEmailValueException.class)
                    .hasMessageContaining("Invalid email format");
        }

        @Test
        @DisplayName("create() rejects email without domain extension")
        void shouldRejectEmailMissingExtension() {
            assertThatThrownBy(() -> Email.create("user@example", VALID_USER_ID))
                    .as("email without extension should be rejected")
                    .isInstanceOf(InvalidEmailValueException.class)
                    .hasMessageContaining("Invalid email format");
        }

        @Test
        @DisplayName("create() accepts email with +, ., _, - in local part and subdomains")
        void shouldAcceptVariousValidEmailFormats() {
            assertThatCode(() -> Email.create("user+tag@example.co.uk", VALID_USER_ID))
                    .as("email with + and subdomain should be accepted")
                    .doesNotThrowAnyException();

            assertThatCode(() -> Email.create("first.last@subdomain.example.com", VALID_USER_ID))
                    .as("email with . in local part and subdomain should be accepted")
                    .doesNotThrowAnyException();

            assertThatCode(() -> Email.create("user_name@example-domain.com", VALID_USER_ID))
                    .as("email with _ in local part and - in domain should be accepted")
                    .doesNotThrowAnyException();
        }
    }

    // =========================================================================
    // Audit Field Validation
    // =========================================================================

    @Nested
    @DisplayName("Validation: Audit Fields")
    class AuditFieldValidation {

        @Test
        @DisplayName("create() rejects null createdBy")
        void shouldRejectNullCreatedBy() {
            assertThatThrownBy(() -> Email.create(VALID_EMAIL, null))
                    .as("null createdBy should throw InvalidAuditFieldException")
                    .isInstanceOf(InvalidAuditFieldException.class)
                    .hasMessageContaining("createdBy")
                    .hasMessageContaining("cannot be null or blank");
        }

        @Test
        @DisplayName("create() rejects empty createdBy")
        void shouldRejectEmptyCreatedBy() {
            assertThatThrownBy(() -> Email.create(VALID_EMAIL, ""))
                    .as("empty createdBy should throw InvalidAuditFieldException")
                    .isInstanceOf(InvalidAuditFieldException.class)
                    .hasMessageContaining("createdBy")
                    .hasMessageContaining("cannot be null or blank");
        }

        @Test
        @DisplayName("create() rejects blank createdBy")
        void shouldRejectBlankCreatedBy() {
            assertThatThrownBy(() -> Email.create(VALID_EMAIL, "   "))
                    .as("blank createdBy should throw InvalidAuditFieldException")
                    .isInstanceOf(InvalidAuditFieldException.class)
                    .hasMessageContaining("createdBy")
                    .hasMessageContaining("cannot be null or blank");
        }
    }

    // =========================================================================
    // Update: Immutability & Behavior
    // =========================================================================

    @Nested
    @DisplayName("Update: New Record Returned")
    class UpdateBehavior {

        @Test
        @DisplayName("update() returns a new record with updated value and actor")
        void shouldReturnNewRecordOnUpdate() throws InterruptedException {
            Email original = Email.create(VALID_EMAIL, VALID_USER_ID);
            Thread.sleep(5);

            Email updated = original.update("new@example.com", "user-456");

            assertThat(updated.value())
                    .as("updated record should have new value")
                    .isEqualTo("new@example.com");
            assertThat(updated.updatedBy())
                    .as("updated record should reflect new actor")
                    .isEqualTo("user-456");
            assertThat(updated.updated())
                    .as("updated timestamp should be after created timestamp")
                    .isAfter(updated.created());
            assertThat(updated.id())
                    .as("identity should be preserved")
                    .isEqualTo(original.id());
            assertThat(updated.created())
                    .as("created timestamp should not change")
                    .isEqualTo(original.created());
            assertThat(updated.createdBy())
                    .as("creator should not change")
                    .isEqualTo(original.createdBy());
        }

        @Test
        @DisplayName("update() does not mutate the original record")
        void shouldNotMutateOriginalRecord() throws InterruptedException {
            Email original = Email.create(VALID_EMAIL, VALID_USER_ID);
            Thread.sleep(5);

            original.update("new@example.com", "user-456");

            assertThat(original.value())
                    .as("original record should be unchanged")
                    .isEqualTo(VALID_EMAIL);
            assertThat(original.updatedBy())
                    .as("original updatedBy should be unchanged")
                    .isEqualTo(VALID_USER_ID);
        }

        @Test
        @DisplayName("update() rejects invalid email")
        void shouldRejectInvalidEmailInUpdate() {
            Email email = Email.create(VALID_EMAIL, VALID_USER_ID);

            assertThatThrownBy(() -> email.update("not-an-email", "user-456"))
                    .as("update with invalid email should throw InvalidEmailValueException")
                    .isInstanceOf(InvalidEmailValueException.class)
                    .hasMessageContaining("Invalid email format");
        }

        @Test
        @DisplayName("update() rejects null updatedBy")
        void shouldRejectNullUpdatedBy() {
            Email email = Email.create(VALID_EMAIL, VALID_USER_ID);

            assertThatThrownBy(() -> email.update("new@example.com", null))
                    .as("update with null updatedBy should throw InvalidAuditFieldException")
                    .isInstanceOf(InvalidAuditFieldException.class)
                    .hasMessageContaining("updatedBy");
        }

        @Test
        @DisplayName("update() rejects blank updatedBy")
        void shouldRejectBlankUpdatedBy() {
            Email email = Email.create(VALID_EMAIL, VALID_USER_ID);

            assertThatThrownBy(() -> email.update("new@example.com", ""))
                    .as("update with blank updatedBy should throw InvalidAuditFieldException")
                    .isInstanceOf(InvalidAuditFieldException.class)
                    .hasMessageContaining("updatedBy");
        }
    }

    // =========================================================================
    // Equality & Object Behavior
    // =========================================================================

    @Nested
    @DisplayName("Object Behavior: Equality & Representation")
    class ObjectBehavior {

        @Test
        @DisplayName("equality is based on id only")
        void shouldUseIdForEquality() {
            Email r1 = Email.create("a@example.com", VALID_USER_ID);
            Email r2 = Email.create("b@example.com", VALID_USER_ID);

            assertThat(r1)
                    .as("records with different ids must not be equal")
                    .isNotEqualTo(r2);
        }

        @Test
        @DisplayName("toString() contains key field information")
        void shouldProduceInformativeToString() {
            Email email = Email.create(VALID_EMAIL, VALID_USER_ID);
            String s = email.toString();

            assertThat(s)
                    .as("toString() should contain record type name")
                    .contains("Email");
            assertThat(s)
                    .as("toString() should contain email value")
                    .contains(VALID_EMAIL);
        }
    }
}
