package hexa.template.ai.email.adapter.rest;

import hexa.template.ai.email.adapter.rest.dto.EmailRequestDto;
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
                    .thenReturn(new CreateEmailRecordResponse("id-123", "user@example.com", now, now));

            String requestBody = """
                {
                  "value": "user@example.com"
                }
                """;
            mockMvc.perform(post("/api/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Last-Modified", "Tue, 19 May 2026 12:00:00 GMT"))
                    .andExpect(jsonPath("$.id").value("id-123"))
                    .andExpect(jsonPath("$.value").value("user@example.com"));
        }
        @Test
        @DisplayName("401 Unauthorized if not authenticated")
        void unauthorized() throws Exception {
            Mockito.when(createEmailRecordUseCase.execute(any(CreateEmailRecordCommand.class)))
                    .thenThrow(new hexa.template.ai.email.application.exceptions.UnauthorizedException("Authentication required"));

            String requestBody = """
                {
                  "value": "user@example.com"
                }
                """;
            mockMvc.perform(post("/api/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
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

            String requestBody = """
                {
                  "value": "user@example.com"
                }
                """;
            mockMvc.perform(post("/api/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.code").value("DUPLICATE_KEY"))
                    .andExpect(jsonPath("$.message").value("duplicate"));
        }
    }
}
