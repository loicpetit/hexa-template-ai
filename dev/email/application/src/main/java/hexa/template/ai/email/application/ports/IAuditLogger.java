package hexa.template.ai.email.application.ports;

import hexa.template.ai.email.application.dto.AuditAction;
import hexa.template.ai.email.domain.entities.Email;

import java.time.Instant;

/**
 * Outbound port interface for audit event logging.
 *
 * Abstracts audit logging implementation from use cases and domain logic. Allows
 * swapping between file-based, database, or external audit service backends without
 * changing business logic.
 *
 * Per TREQ-0005: Audit logging is a secondary concern. Failures in audit logging
 * must not fail the primary operation. Implementations must catch and suppress errors.
 *
 * Implementations: SyslogAuditLogger, DatabaseAuditLogger, FileAuditLogger
 */
public interface IAuditLogger {

    /**
     * Logs an audit event for a business operation on an email record.
     *
     * Records the authenticated user (actor) who performed an action (CREATE, UPDATE, DELETE)
     * on a specific email record at a specific timestamp. Audit events are used for
     * compliance, forensic investigation, and user activity tracking.
     *
     * This method is non-blocking and should not throw exceptions. Any errors in persisting
     * the audit event are caught and logged internally; audit failures do not fail the
     * primary operation.
     *
     * @param action    the business action performed (CREATE, UPDATE, or DELETE)
     * @param actor     the id of the authenticated user who performed the action
     * @param email     the affected email record (may be pre-update or pre-delete state)
     * @param timestamp the UTC timestamp when the action occurred
     */
    void log(AuditAction action, String actor, Email email, Instant timestamp);
}
