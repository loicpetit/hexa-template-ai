package hexa.template.ai.email.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for User record.
 *
 * Verifies User construction, field access, and null/blank safety.
 */
@DisplayName("User Record")
class UserTest {

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("User with name only")
        void construct_with_name() {
            var user = new User("John Doe");

            assertThat(user.name()).as("name should match").isEqualTo("John Doe");
            assertThat(user.roles()).as("roles should be empty list by default").isEmpty();
        }

        @Test
        @DisplayName("User with name and roles")
        void construct_with_name_and_roles() {
            var roles = List.of("ADMIN", "USER");
            var user = new User("Jane Doe", roles);

            assertThat(user.name()).as("name should match").isEqualTo("Jane Doe");
            assertThat(user.roles()).as("roles should match").isEqualTo(roles);
        }
    }

    @Nested
    @DisplayName("Null and Blank Safety")
    class NullAndBlankSafety {

        @Test
        @DisplayName("Null name throws IllegalArgumentException")
        void null_name_throws() {
            assertThatThrownBy(() -> new User(null, List.of()))
                    .as("name must not be null")
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Blank name throws IllegalArgumentException")
        void blank_name_throws() {
            assertThatThrownBy(() -> new User("   ", List.of()))
                    .as("name must not be blank")
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Null roles throws IllegalArgumentException")
        void null_roles_throws() {
            assertThatThrownBy(() -> new User("John Doe", null))
                    .as("roles must not be null")
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
