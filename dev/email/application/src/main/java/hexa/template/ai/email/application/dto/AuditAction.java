package hexa.template.ai.email.application.dto;

/**
 * Enumeration of audit-relevant business actions.
 *
 * Per TREQ-0005: Audit logger records the type of action performed (CREATE, UPDATE, DELETE)
 * along with actor, entity, and timestamp for compliance and forensic investigation.
 */
public enum AuditAction {
    /**
     * Email record created.
     */
    CREATE,

    /**
     * Email record updated.
     */
    UPDATE,

    /**
     * Email record deleted.
     */
    DELETE
}
