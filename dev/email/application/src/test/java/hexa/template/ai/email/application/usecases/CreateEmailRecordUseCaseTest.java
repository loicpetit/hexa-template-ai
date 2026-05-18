package hexa.template.ai.email.application.usecases;

import hexa.template.ai.email.application.dto.AuditAction;
import hexa.template.ai.email.application.dto.User;
import hexa.template.ai.email.application.exceptions.DuplicateKeyException;
import hexa.template.ai.email.application.exceptions.UnauthorizedException;
import hexa.template.ai.email.application.ports.IAuditLogger;
import hexa.template.ai.email.application.ports.IAuthProvider;
import hexa.template.ai.email.application.ports.IEmailRepository;
import hexa.template.ai.email.domain.entities.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateEmailRecordUseCase")
class CreateEmailRecordUseCaseTest {

    @Mock
    private IAuthProvider authProvider;

    @Mock
    private IEmailRepository emailRepository;

    @Mock
    private IAuditLogger auditLogger;

    private CreateEmailRecordUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateEmailRecordUseCase(authProvider, emailRepository, auditLogger);
    }

    @Test
    @DisplayName("Authenticated user creates email, persists it, audits it, and gets a response")
    void execute_authenticatedUser_createsEmail() {
        User user = new User("alice");
        Email savedEmail = new Email(
                "email-123",
                "user@example.com",
                Instant.parse("2026-05-18T10:15:30Z"),
                Instant.parse("2026-05-18T10:15:30Z"),
                "alice",
                "alice"
        );

        when(authProvider.getCurrentUser()).thenReturn(Optional.of(user));
        when(emailRepository.save(any(Email.class))).thenReturn(savedEmail);

        CreateEmailRecordResponse response = useCase.execute(new CreateEmailRecordCommand("user@example.com"));

        assertThat(response.id()).isEqualTo("email-123");
        assertThat(response.value()).isEqualTo("user@example.com");
        assertThat(response.created()).isEqualTo(savedEmail.created());

        verify(emailRepository).save(any(Email.class));
        verify(auditLogger).log(eq(AuditAction.CREATE), eq("alice"), eq(savedEmail), eq(savedEmail.created()));
    }

    @Test
    @DisplayName("Unauthenticated user gets UnauthorizedException")
    void execute_unauthenticatedUser_throwsUnauthorized() {
        when(authProvider.getCurrentUser()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new CreateEmailRecordCommand("user@example.com")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Authentication required");

        verify(emailRepository, never()).save(any(Email.class));
        verify(auditLogger, never()).log(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Duplicate email propagates DuplicateKeyException")
    void execute_duplicateValue_propagatesDuplicateKey() {
        when(authProvider.getCurrentUser()).thenReturn(Optional.of(new User("alice")));
        when(emailRepository.save(any(Email.class))).thenThrow(new DuplicateKeyException("duplicate"));

        assertThatThrownBy(() -> useCase.execute(new CreateEmailRecordCommand("user@example.com")))
                .isInstanceOf(DuplicateKeyException.class)
                .hasMessage("duplicate");

        verify(auditLogger, never()).log(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Audit logger failure does not fail the use case")
    void execute_auditFailure_isNonBlocking() {
        User user = new User("alice");
        Email savedEmail = new Email(
                "email-123",
                "user@example.com",
                Instant.parse("2026-05-18T10:15:30Z"),
                Instant.parse("2026-05-18T10:15:30Z"),
                "alice",
                "alice"
        );

        when(authProvider.getCurrentUser()).thenReturn(Optional.of(user));
        when(emailRepository.save(any(Email.class))).thenReturn(savedEmail);
        org.mockito.Mockito.doThrow(new RuntimeException("audit down"))
                .when(auditLogger).log(any(AuditAction.class), any(String.class), any(Email.class), any(Instant.class));

        CreateEmailRecordResponse response = useCase.execute(new CreateEmailRecordCommand("user@example.com"));

        assertThat(response.id()).isEqualTo(savedEmail.id());
        assertThat(response.value()).isEqualTo(savedEmail.value());
        assertThat(response.created()).isEqualTo(savedEmail.created());

        verify(auditLogger).log(eq(AuditAction.CREATE), eq("alice"), eq(savedEmail), eq(savedEmail.created()));
    }
}
