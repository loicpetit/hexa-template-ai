package hexa.template.ai.email.application.usecases;

import hexa.template.ai.email.application.dto.AuditAction;
import hexa.template.ai.email.application.dto.User;
import hexa.template.ai.email.application.exceptions.UnauthorizedException;
import hexa.template.ai.email.application.ports.IAuditLogger;
import hexa.template.ai.email.application.ports.IAuthProvider;
import hexa.template.ai.email.application.ports.IEmailRepository;
import hexa.template.ai.email.domain.entities.Email;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class CreateEmailRecordUseCase {

    private final IAuthProvider authProvider;
    private final IEmailRepository emailRepository;
    private final IAuditLogger auditLogger;

    public CreateEmailRecordUseCase(
            IAuthProvider authProvider,
            IEmailRepository emailRepository,
            IAuditLogger auditLogger
    ) {
        this.authProvider = Objects.requireNonNull(authProvider, "authProvider must not be null");
        this.emailRepository = Objects.requireNonNull(emailRepository, "emailRepository must not be null");
        this.auditLogger = Objects.requireNonNull(auditLogger, "auditLogger must not be null");
    }

    public CreateEmailRecordResponse execute(CreateEmailRecordCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        User currentUser = authProvider.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));

        Email email = Email.create(command.value(), currentUser.name());
        Email savedEmail = emailRepository.save(email);

        try {
            auditLogger.log(AuditAction.CREATE, currentUser.name(), savedEmail, savedEmail.created());
        } catch (RuntimeException exception) {
            log.warn("Audit logging failed for CREATE on email id {}", savedEmail.id(), exception);
        }

        return new CreateEmailRecordResponse(savedEmail.id(), savedEmail.value(), savedEmail.created());
    }
}