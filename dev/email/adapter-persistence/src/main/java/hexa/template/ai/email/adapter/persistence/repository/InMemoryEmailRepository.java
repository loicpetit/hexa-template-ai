package hexa.template.ai.email.adapter.persistence.repository;

import hexa.template.ai.email.application.exceptions.DuplicateKeyException;
import hexa.template.ai.email.application.exceptions.EntityNotFoundException;
import hexa.template.ai.email.application.ports.IEmailRepository;
import hexa.template.ai.email.domain.entities.Email;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository adapter for email records.
 *
 * Implements the IEmailRepository port using a process-scoped ConcurrentHashMap.
 * Enforces uniqueness of both id and email value. Data is not durable (lost on
 * process restart); this is accepted for the current scope per TREQ-0006.
 *
 * Thread-safe: uses ConcurrentHashMap for concurrent reads; write operations
 * use synchronized blocks to atomically check-and-update uniqueness constraints.
 *
 * Designed for Spring Boot dependency injection. A single instance will typically
 * be managed by Spring as a bean, providing process-scoped shared state.
 */
public class InMemoryEmailRepository implements IEmailRepository {

    private final ConcurrentHashMap<String, Email> store;
    private final ConcurrentHashMap<String, String> valueToIdIndex; // Maps email value → id for uniqueness

    public InMemoryEmailRepository() {
        this.store = new ConcurrentHashMap<>();
        this.valueToIdIndex = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized Email save(Email email) {
        if (email == null) {
            throw new NullPointerException("Email must not be null");
        }

        String value = email.value();

        // Check if value already exists (case-sensitive)
        if (valueToIdIndex.containsKey(value)) {
            throw new DuplicateKeyException("Email record with value '" + value + "' already exists");
        }

        // Generate id if not already assigned
        Email emailWithId = generateIdIfNecessary(email);

        // Check if id already exists (for updates or duplicates)
        if (store.containsKey(emailWithId.id())) {
            throw new DuplicateKeyException("Email record with id '" + emailWithId.id() + "' already exists");
        }

        // Atomically persist both maps
        store.put(emailWithId.id(), emailWithId);
        valueToIdIndex.put(value, emailWithId.id());

        return emailWithId;
    }

    /**
     * Generates a UUID id for an email if it doesn't already have one.
     *
     * @param email the email record (may have null id)
     * @return the same email if it already has an id, or a new email with generated id
     */
    private Email generateIdIfNecessary(Email email) {
        if (email.id() != null) {
            return email;
        }
        String generatedId = UUID.randomUUID().toString();
        return new Email(generatedId, email.value(), email.created(), email.updated(), email.createdBy(), email.updatedBy());
    }

    @Override
    public Email findById(String id) {
        if (id == null) {
            throw new NullPointerException("Email id must not be null");
        }
        return store.get(id); // Returns null if not found
    }

    @Override
    public List<Email> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public synchronized void delete(String id) {
        if (id == null) {
            throw new NullPointerException("Email id must not be null");
        }

        Email email = store.remove(id);
        if (email == null) {
            throw new EntityNotFoundException("Email record with id '" + id + "' not found");
        }

        // Also remove from value index
        valueToIdIndex.remove(email.value());
    }
}
