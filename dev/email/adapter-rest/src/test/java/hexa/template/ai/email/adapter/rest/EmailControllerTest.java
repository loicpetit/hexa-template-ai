package hexa.template.ai.email.adapter.rest;

import hexa.template.ai.email.domain.exceptions.InvalidEmailValueException;
import hexa.template.ai.email.application.usecases.CreateEmailRecordCommand;
import hexa.template.ai.email.application.usecases.CreateEmailRecordResponse;
import hexa.template.ai.email.application.usecases.CreateEmailRecordUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmailController.class)
class EmailControllerTest {
    private static final String URL = "/api/emails";
        public static final String VALID_EMAIL = "user@example.com";
        public static final String TEST_API_KEY = "test-api-key";
        public static final String VALID_REQUEST_BODY = """
                        {
                            \"value\": \"user@example.com\"
                        }
                        """;
        public static final String BLANK_VALUE_REQUEST_BODY = """
                        {
                            \"value\": \"\"
                        }
                        """;

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    CreateEmailRecordUseCase createEmailRecordUseCase;

    @Nested
    @DisplayName("POST /api/emails")
    class Create {
        @Test
        @DisplayName("201 Created on success")
        void success() throws Exception {
            Instant now = Instant.parse("2026-05-19T12:00:00Z");
            Mockito.when(createEmailRecordUseCase.execute(any(CreateEmailRecordCommand.class)))
                .thenReturn(new CreateEmailRecordResponse("id-123", VALID_EMAIL, now, now));

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST_BODY))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Last-Modified", "Tue, 19 May 2026 12:00:00 GMT"))
                    .andExpect(jsonPath("$.id").value("id-123"))
                .andExpect(jsonPath("$.value").value(VALID_EMAIL));
        }
        @Test
        @DisplayName("401 Unauthorized if not authenticated")
        void unauthorized() throws Exception {
            Mockito.when(createEmailRecordUseCase.execute(any(CreateEmailRecordCommand.class)))
                    .thenThrow(new hexa.template.ai.email.application.exceptions.UnauthorizedException("Authentication required"));

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST_BODY))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("Authentication required"));
        }

        @Test
        @DisplayName("409 Conflict if duplicate email")
        void duplicate() throws Exception {
            Mockito.when(createEmailRecordUseCase.execute(any(CreateEmailRecordCommand.class)))
                    .thenThrow(new hexa.template.ai.email.application.exceptions.DuplicateKeyException("duplicate"));

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST_BODY))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.code").value("DUPLICATE_KEY"))
                    .andExpect(jsonPath("$.message").value("duplicate"));
        }

        @Test
        @DisplayName("400 Bad Request on invalid input")
        void invalidInput() throws Exception {
            Mockito.when(createEmailRecordUseCase.execute(any(CreateEmailRecordCommand.class)))
                .thenThrow(new InvalidEmailValueException("Email value cannot be null or blank"));

            mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(BLANK_VALUE_REQUEST_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("Email value cannot be null or blank"));
        }
    }
}
