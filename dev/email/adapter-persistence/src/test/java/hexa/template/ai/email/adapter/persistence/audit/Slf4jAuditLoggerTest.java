package hexa.template.ai.email.adapter.persistence.audit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import hexa.template.ai.email.application.dto.AuditAction;
import hexa.template.ai.email.domain.entities.Email;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class Slf4jAuditLoggerTest {

    private Slf4jAuditLogger auditLogger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        auditLogger = new Slf4jAuditLogger();
        Logger logger = (Logger) LoggerFactory.getLogger(Slf4jAuditLogger.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(Slf4jAuditLogger.class);
        logger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Nested
    @DisplayName("log()")
    class Log {
        @Test
        @DisplayName("logs structured audit message at INFO level")
        void logsStructuredMessage() {
            Instant now = Instant.parse("2026-05-19T12:00:00Z");
            Email email = new Email("id-123", "user@example.com", now, now, "alice", "alice");

            auditLogger.log(AuditAction.CREATE, "alice", email, now);

            List<ILoggingEvent> logs = listAppender.list;
            assertThat(logs).hasSize(1);
            assertThat(logs.get(0).getLevel()).isEqualTo(Level.INFO);
            assertThat(logs.get(0).getFormattedMessage())
                    .contains("action=CREATE")
                    .contains("actor=alice")
                    .contains("emailId=id-123")
                    .contains("emailValue=user@example.com")
                    .contains("timestamp=2026-05-19T12:00:00Z");
        }

        @Test
        @DisplayName("does not propagate exceptions")
        void doesNotPropagateExceptions() {
            // Pass null email to trigger an exception inside log()
            assertThatNoException().isThrownBy(() ->
                auditLogger.log(AuditAction.CREATE, "alice", null, Instant.now())
            );
        }
    }
}
