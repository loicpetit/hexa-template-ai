package hexa.template.ai.email.adapter.rest.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hexa.template.ai.email.application.dto.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ApiKeyAuthProvider")
class ApiKeyAuthProviderTest {

    private Map<String, User> keyMap;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setup() {
        keyMap = new HashMap<>();
        keyMap.put("valid-key-alice", new User("alice", List.of("admin")));
        keyMap.put("valid-key-bob", new User("bob", List.of("user")));
        mockRequest = mock(HttpServletRequest.class);
    }

    @Nested
    @DisplayName("Header Parsing")
    class HeaderParsing {

        @Test
        @DisplayName("Valid ApiKey token returns correct user")
        void validApiKeyToken() {
            when(mockRequest.getHeader("Authorization")).thenReturn("ApiKey valid-key-alice");
            ApiKeyAuthProvider provider = new ApiKeyAuthProvider(keyMap, mockRequest);

            Optional<User> result = provider.getCurrentUser();

            assertThat(result).isPresent().as("User should be resolved for valid ApiKey token");
            assertThat(result.get().name()).isEqualTo("alice").as("User name should match mapped user");
        }

        @Test
        @DisplayName("Bearer prefix is not supported, returns empty")
        void bearerPrefixNotSupported() {
            when(mockRequest.getHeader("Authorization")).thenReturn("Bearer valid-key-bob");
            ApiKeyAuthProvider provider = new ApiKeyAuthProvider(keyMap, mockRequest);

            Optional<User> result = provider.getCurrentUser();

            assertThat(result).as("User should not be resolved for Bearer prefix (not supported)").isEmpty();
        }

        @Test
        @DisplayName("Malformed header (no prefix) returns empty")
        void malformedHeaderNegativePrefix() {
            when(mockRequest.getHeader("Authorization")).thenReturn("valid-key-alice");
            ApiKeyAuthProvider provider = new ApiKeyAuthProvider(keyMap, mockRequest);

            Optional<User> result = provider.getCurrentUser();

            assertThat(result).as("User should not be resolved for malformed header without prefix").isEmpty();
        }

        @Test
        @DisplayName("Missing Authorization header returns empty")
        void missingAuthorizationHeader() {
            when(mockRequest.getHeader("Authorization")).thenReturn(null);
            ApiKeyAuthProvider provider = new ApiKeyAuthProvider(keyMap, mockRequest);

            Optional<User> result = provider.getCurrentUser();

            assertThat(result).as("User should not be resolved when Authorization header is missing").isEmpty();
        }
    }

    @Nested
    @DisplayName("Key Validation")
    class KeyValidation {

        @Test
        @DisplayName("Invalid/unknown key returns empty")
        void invalidKey() {
            when(mockRequest.getHeader("Authorization")).thenReturn("ApiKey unknown-key");
            ApiKeyAuthProvider provider = new ApiKeyAuthProvider(keyMap, mockRequest);

            Optional<User> result = provider.getCurrentUser();

            assertThat(result).as("User should not be resolved for unknown key").isEmpty();
        }

        @Test
        @DisplayName("Empty key map returns empty for any key")
        void emptyKeyMap() {
            when(mockRequest.getHeader("Authorization")).thenReturn("ApiKey valid-key-alice");
            ApiKeyAuthProvider provider = new ApiKeyAuthProvider(new HashMap<>(), mockRequest);

            Optional<User> result = provider.getCurrentUser();

            assertThat(result).as("User should not be resolved when key map is empty").isEmpty();
        }
    }

    @Nested
    @DisplayName("Request Handling")
    class RequestHandling {

        @Test
        @DisplayName("Null request returns empty")
        void nullRequest() {
            ApiKeyAuthProvider provider = new ApiKeyAuthProvider(keyMap, null);

            Optional<User> result = provider.getCurrentUser();

            assertThat(result).as("User should not be resolved when request is null").isEmpty();
        }

        @Test
        @DisplayName("Blank Authorization header returns empty")
        void blankAuthorizationHeader() {
            when(mockRequest.getHeader("Authorization")).thenReturn("   ");
            ApiKeyAuthProvider provider = new ApiKeyAuthProvider(keyMap, mockRequest);

            Optional<User> result = provider.getCurrentUser();

            assertThat(result).as("User should not be resolved when Authorization header is blank").isEmpty();
        }
    }

    @Nested
    @DisplayName("Constructor Validation")
    class ConstructorValidation {

        @Test
        @DisplayName("Null key map throws IllegalArgumentException")
        void nullKeyMap() {
            try {
                new ApiKeyAuthProvider(null, mockRequest);
                assertThat(false).as("Should have thrown IllegalArgumentException").isTrue();
            } catch (IllegalArgumentException e) {
                assertThat(e.getMessage()).isEqualTo("API key to user map must not be null");
            }
        }
    }
}
