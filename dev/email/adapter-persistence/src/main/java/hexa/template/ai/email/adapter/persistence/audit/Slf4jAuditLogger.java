package hexa.template.ai.email.adapter.persistence.audit;

import hexa.template.ai.email.application.dto.AuditAction;
import hexa.template.ai.email.application.ports.IAuditLogger;
import hexa.template.ai.email.domain.entities.Email;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
public class Slf4jAuditLogger implements IAuditLogger {

    @Override
    public void log(AuditAction action, String actor, Email email, Instant timestamp) {
        try {
            String ts = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC).format(timestamp);
            log.info("AUDIT action={} actor={} emailId={} emailValue={} timestamp={}",
                    action, actor, email.id(), email.value(), ts);
        } catch (Exception e) {
            log.warn("Audit logging failed silently", e);
        }
    }
}
