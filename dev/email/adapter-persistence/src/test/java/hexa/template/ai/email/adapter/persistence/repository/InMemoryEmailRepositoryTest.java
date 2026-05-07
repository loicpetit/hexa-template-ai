package hexa.template.ai.email.adapter.persistence.repository;

import hexa.template.ai.email.application.exceptions.DuplicateKeyException;
import hexa.template.ai.email.application.exceptions.EntityNotFoundException;
import hexa.template.ai.email.domain.entities.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for InMemoryEmailRepository.
 *
 * Verifies that the in-memory singleton repository enforces uniqueness constraints,
 * handles CRUD operations correctly, and maps errors appropriately per TREQ-0006.
 *
 * Note: Singleton state is cleared before each test to ensure isolation.
 */
@DisplayName("InMemoryEmailRepository")
class InMemoryEmailRepositoryTest {

    private InMemoryEmailRepository repository;
    private Email validEmail;

    @BeforeEach
    void setUp() {
        repository = new InMemoryEmailRepository(); // Fresh instance per test

        // Create a valid test email
        validEmail = Email.create("test@example.com", "test-user");
    }

    @Nested
    @DisplayName("Save Operation")
    class SaveOperation {

        @Test
        @DisplayName("Save new email assigns id and succeeds")
        void save_new_email_assigns_id() {
            var email = Email.create("test@example.com", "test-user");
            assertThat(email.id()).as("email should have null id before save").isNull();

            var saved = repository.save(email);

            assertThat(saved)
                    .as("saved email should be returned with id assigned")
                    .isNotNull();
            assertThat(saved.id())
                    .as("saved email should have a non-null id assigned by repository")
                    .isNotBlank();
            assertThat(saved.value())
                    .as("email value should be preserved")
                    .isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Save with duplicate value throws DuplicateKeyException")
        void save_duplicate_value_throws() {
            var email1 = Email.create("test@example.com", "user1");
            repository.save(email1);

            var email2 = Email.create("test@example.com", "user2");
            assertThatThrownBy(() -> repository.save(email2))
                    .as("duplicate value should throw DuplicateKeyException")
                    .isInstanceOf(DuplicateKeyException.class)
                    .hasMessageContaining("test@example.com");
        }

        @Test
        @DisplayName("Save null email throws NullPointerException")
        void save_null_throws() {
            assertThatThrownBy(() -> repository.save(null))
                    .as("null email should throw NullPointerException")
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Save multiple emails with different values succeeds and assigns unique ids")
        void save_multiple_different_values_succeeds() {
            var email1 = Email.create("alice@example.com", "alice");
            var email2 = Email.create("bob@example.com", "bob");

            var saved1 = repository.save(email1);
            var saved2 = repository.save(email2);

            assertThat(saved1.id())
                    .as("first email should have assigned id")
                    .isNotBlank();
            assertThat(saved2.id())
                    .as("second email should have assigned id")
                    .isNotBlank();
            assertThat(saved1.id())
                    .as("ids should be unique")
                    .isNotEqualTo(saved2.id());
            assertThat(repository.findAll()).as("both emails in store").hasSize(2);
        }
    }

    @Nested
    @DisplayName("Find By ID Operation")
    class FindByIdOperation {

        @Test
        @DisplayName("Find existing email by id succeeds")
        void find_existing_succeeds() {
            var saved = repository.save(validEmail);

            var found = repository.findById(saved.id());

            assertThat(found)
                    .as("email should be found by id")
                    .isNotNull()
                    .isEqualTo(saved);
        }

        @Test
        @DisplayName("Find non-existing email by id returns null")
        void find_non_existing_returns_null() {
            var found = repository.findById("non-existent-id");

            assertThat(found)
                    .as("non-existent email should return null")
                    .isNull();
        }

        @Test
        @DisplayName("Find by null id throws NullPointerException")
        void find_null_id_throws() {
            assertThatThrownBy(() -> repository.findById(null))
                    .as("null id should throw NullPointerException")
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Find after save returns the saved instance with assigned id")
        void find_after_save_returns_same() {
            var saved = repository.save(validEmail);

            var found = repository.findById(saved.id());

            assertThat(found)
                    .as("found email should match saved email")
                    .isEqualTo(saved);
        }
    }

    @Nested
    @DisplayName("Find All Operation")
    class FindAllOperation {

        @Test
        @DisplayName("Find all on empty repository returns empty list")
        void find_all_empty_returns_empty_list() {
            var all = repository.findAll();

            assertThat(all)
                    .as("empty repository should return empty list")
                    .isEmpty();
        }

        @Test
        @DisplayName("Find all with one email returns list with one email")
        void find_all_single_email() {
            var saved = repository.save(validEmail);

            var all = repository.findAll();

            assertThat(all)
                    .as("should return list with one email")
                    .hasSize(1)
                    .contains(saved);
        }

        @Test
        @DisplayName("Find all with multiple emails returns all")
        void find_all_multiple_emails() {
            var email1 = Email.create("alice@example.com", "alice");
            var email2 = Email.create("bob@example.com", "bob");
            var email3 = Email.create("charlie@example.com", "charlie");

            var saved1 = repository.save(email1);
            var saved2 = repository.save(email2);
            var saved3 = repository.save(email3);

            var all = repository.findAll();

            assertThat(all)
                    .as("should return all emails")
                    .hasSize(3)
                    .containsExactlyInAnyOrder(saved1, saved2, saved3);
        }

        @Test
        @DisplayName("Find all returns new list (not reference to internal store)")
        void find_all_returns_independent_list() {
            var saved = repository.save(validEmail);

            var list1 = repository.findAll();
            var list2 = repository.findAll();

            assertThat(list1)
                    .as("each call should return a new list")
                    .isNotSameAs(list2);
        }
    }

    @Nested
    @DisplayName("Delete Operation")
    class DeleteOperation {

        @Test
        @DisplayName("Delete existing email succeeds")
        void delete_existing_succeeds() {
            var saved = repository.save(validEmail);

            assertThatCode(() -> repository.delete(saved.id()))
                    .as("deleting existing email should not throw")
                    .doesNotThrowAnyException();

            var found = repository.findById(saved.id());
            assertThat(found)
                    .as("email should be gone after delete")
                    .isNull();
        }

        @Test
        @DisplayName("Delete non-existing email throws EntityNotFoundException")
        void delete_non_existing_throws() {
            assertThatThrownBy(() -> repository.delete("non-existent-id"))
                    .as("deleting non-existent email should throw EntityNotFoundException")
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("non-existent-id");
        }

        @Test
        @DisplayName("Delete null id throws NullPointerException")
        void delete_null_id_throws() {
            assertThatThrownBy(() -> repository.delete(null))
                    .as("null id should throw NullPointerException")
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Delete also removes email value from uniqueness index")
        void delete_removes_from_value_index() {
            var saved = repository.save(validEmail);
            repository.delete(saved.id());

            // Should be able to save a new email with the same value
            var newEmail = Email.create("test@example.com", "new-user");
            assertThatCode(() -> repository.save(newEmail))
                    .as("after deletion, same value should be reusable")
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Delete one email does not affect others")
        void delete_does_not_affect_others() {
            var email1 = Email.create("alice@example.com", "alice");
            var email2 = Email.create("bob@example.com", "bob");

            var saved1 = repository.save(email1);
            var saved2 = repository.save(email2);

            repository.delete(saved1.id());

            var all = repository.findAll();
            assertThat(all)
                    .as("only email2 should remain")
                    .hasSize(1)
                    .contains(saved2);
        }
    }


}
