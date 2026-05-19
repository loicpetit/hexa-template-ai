package hexa.template.ai.email.adapter.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EmailControllerE2ETest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Nested
    @DisplayName("POST /api/emails")
    class CreateEmail {
                @Test
                @DisplayName("should create email with valid API key")
                void shouldCreateEmailWithValidApiKey() {
                        String requestBody = EmailControllerTest.VALID_REQUEST_BODY;
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.set(HttpHeaders.AUTHORIZATION, "ApiKey " + EmailControllerTest.TEST_API_KEY);
                        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

                        ResponseEntity<Map> response = restTemplate.postForEntity(url("/api/emails"), entity, Map.class);

                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody()).containsKeys("id", "value");
                        assertThat(response.getBody().get("value")).isEqualTo(EmailControllerTest.VALID_EMAIL);
                }

                @Test
                @DisplayName("should reject request with missing API key")
                void shouldRejectMissingApiKey() {
                        String requestBody = EmailControllerTest.VALID_REQUEST_BODY;
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

                        ResponseEntity<Map> response = restTemplate.postForEntity(url("/api/emails"), entity, Map.class);

                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody()).containsEntry("status", 401);
                        assertThat(response.getBody()).containsEntry("code", "UNAUTHORIZED");
                }

                @Test
                @DisplayName("should reject request with blank value")
                void shouldRejectBlankValue() {
                        String requestBody = EmailControllerTest.BLANK_VALUE_REQUEST_BODY;
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.set(HttpHeaders.AUTHORIZATION, "ApiKey " + EmailControllerTest.TEST_API_KEY);
                        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

                        ResponseEntity<Map> response = restTemplate.postForEntity(url("/api/emails"), entity, Map.class);

                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody()).containsEntry("status", 400);
                        assertThat(response.getBody()).containsEntry("code", "INVALID_INPUT");
                }
    }
}
