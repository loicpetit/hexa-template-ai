package hexa.template.ai.email.application.ports;

import hexa.template.ai.email.application.exceptions.DuplicateKeyException;
import hexa.template.ai.email.application.exceptions.EntityNotFoundException;
import hexa.template.ai.email.domain.entities.Email;

import java.util.List;

/**
 * Outbound port interface for email record persistence.
 *
 * Abstracts data persistence implementation from use cases and domain logic. Allows
 * swapping between in-memory, relational database, or other storage backends without
 * changing business logic.
 *
 * Per TREQ-0006: Repository enforces id and email value uniqueness. All storage errors
 * are mapped to domain exceptions; no implementation details are leaked to callers.
 *
 * Implementations: InMemoryEmailRepository (process-scoped), DatabaseEmailRepository (future)
 */
public interface IEmailRepository {

    /**
     * Saves an email record to the repository.
     *
     * If the email record id already exists, throws DuplicateKeyException to prevent
     * overwriting existing records.
     *
     * If the email record value (email address) is already present, throws DuplicateKeyException
     * to enforce uniqueness of email values.
     *
     * @param email the email record to save (must not be null)
     * @return the saved email record (typically the same instance)
     * @throws DuplicateKeyException if id or value already exists in the repository
     * @throws NullPointerException if email is null
     */
    Email save(Email email);

    /**
     * Retrieves an email record by its unique identifier.
     *
     * @param id the email record id to search for (must not be null)
     * @return the email record if found, null if not found
     * @throws NullPointerException if id is null
     */
    Email findById(String id);

    /**
     * Retrieves all email records from the repository.
     *
     * @return a list of all email records; empty list if none exist
     */
    List<Email> findAll();

    /**
     * Deletes an email record by its unique identifier.
     *
     * If no record with the given id exists, throws EntityNotFoundException.
     *
     * @param id the email record id to delete (must not be null)
     * @throws EntityNotFoundException if no record with the given id exists
     * @throws NullPointerException if id is null
     */
    void delete(String id);
}
