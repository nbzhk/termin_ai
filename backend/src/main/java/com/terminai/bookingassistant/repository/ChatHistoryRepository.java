package com.terminai.bookingassistant.repository;

import com.terminai.bookingassistant.entity.ChatHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ChatHistory} entities.
 *
 * <p>The primary access pattern is loading the most recent N messages for a given
 * tenant + customer pair to reconstruct the LLM context window.
 */
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, UUID> {

    /**
     * Returns the most recent messages for a tenant and customer, ordered newest first.
     *
     * <p>Use {@link Pageable} to limit the window size (e.g., last 20 messages):
     * {@code PageRequest.of(0, 20, Sort.by("createdAt").descending())}
     *
     * @param tenantId            the owning tenant's UUID
     * @param customerPhoneNumber the customer's E.164 phone number
     * @param pageable            pagination / size limit
     * @return ordered list of chat history records
     */
    List<ChatHistory> findByTenantIdAndCustomerPhoneNumberOrderByCreatedAtDesc(
            UUID tenantId,
            String customerPhoneNumber,
            Pageable pageable
    );
}
