package com.terminai.bookingassistant.service;

import com.terminai.bookingassistant.dto.TenantConfig;
import com.terminai.bookingassistant.dto.WhatsAppWebhookPayload;
import com.terminai.bookingassistant.entity.Tenant;
import com.terminai.bookingassistant.exception.TenantNotFoundException;
import com.terminai.bookingassistant.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Core multi-tenant routing service.
 *
 * <p>Resolves the correct tenant configuration from an inbound WhatsApp webhook payload
 * by extracting the destination {@code phone_number_id} and querying the {@code tenants} table.
 *
 * <p>The routing key is {@code entry[0].changes[0].value.metadata.phone_number_id},
 * which is the Meta WhatsApp Business Phone Number ID. This is stable, globally unique
 * within Meta's platform, and present in every inbound message event.
 */
@Service
public class TenantRoutingService {

    private final TenantRepository tenantRepository;

    public TenantRoutingService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Resolves the tenant configuration for an inbound WhatsApp message.
     *
     * <p>Extracts the {@code phone_number_id} from the webhook payload, looks up the
     * matching active tenant in the database, and returns a {@link TenantConfig} DTO.
     *
     * @param payload the parsed WhatsApp webhook payload
     * @return resolved {@link TenantConfig} for the destination business
     * @throws TenantNotFoundException  if no active tenant matches the phone number ID
     * @throws IllegalArgumentException if the payload does not contain a valid phone_number_id
     */
    public TenantConfig resolveTenant(WhatsAppWebhookPayload payload) {
        String phoneNumberId = extractPhoneNumberId(payload);

        Tenant tenant = tenantRepository
                .findByWhatsappPhoneNumberIdAndActiveTrue(phoneNumberId)
                .orElseThrow(() -> new TenantNotFoundException(
                        "No active tenant found for WhatsApp phone_number_id: " + phoneNumberId
                ));

        return toTenantConfig(tenant);
    }

    // -------------------------------------------------------------------------
    // Payload extraction helpers
    // -------------------------------------------------------------------------

    /**
     * Extracts the Meta Business Phone Number ID from the webhook payload.
     * Location: {@code entry[0].changes[0].value.metadata.phone_number_id}
     *
     * @throws IllegalArgumentException if the payload structure is missing or malformed
     */
    public String extractPhoneNumberId(WhatsAppWebhookPayload payload) {
        try {
            List<WhatsAppWebhookPayload.Entry> entries = payload.getEntry();
            if (entries == null || entries.isEmpty()) {
                throw new IllegalArgumentException("Webhook payload contains no entries");
            }

            List<WhatsAppWebhookPayload.Change> changes = entries.get(0).getChanges();
            if (changes == null || changes.isEmpty()) {
                throw new IllegalArgumentException("Webhook payload entry contains no changes");
            }

            WhatsAppWebhookPayload.Metadata metadata = changes.get(0).getValue().getMetadata();
            if (metadata == null || metadata.getPhoneNumberId() == null) {
                throw new IllegalArgumentException(
                        "phone_number_id is missing in webhook payload metadata"
                );
            }

            return metadata.getPhoneNumberId();

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to extract phone_number_id from webhook payload", e
            );
        }
    }

    /**
     * Extracts the customer's WhatsApp phone number ("from" field).
     * Location: {@code entry[0].changes[0].value.messages[0].from}
     *
     * @return the customer phone number, or {@code null} if not present
     *         (e.g. status update events contain no messages array)
     */
    public String extractCustomerPhoneNumber(WhatsAppWebhookPayload payload) {
        try {
            List<WhatsAppWebhookPayload.Message> messages = getMessages(payload);
            if (messages == null || messages.isEmpty()) {
                return null;
            }
            return messages.get(0).getFrom();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the plain text body from the first inbound message.
     * Location: {@code entry[0].changes[0].value.messages[0].text.body}
     *
     * @return the message body, or {@code null} for non-text message types
     */
    public String extractMessageText(WhatsAppWebhookPayload payload) {
        try {
            List<WhatsAppWebhookPayload.Message> messages = getMessages(payload);
            if (messages == null || messages.isEmpty()) {
                return null;
            }
            WhatsAppWebhookPayload.TextContent text = messages.get(0).getText();
            return text != null ? text.getBody() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the Meta-assigned message ID (wamid).
     * Location: {@code entry[0].changes[0].value.messages[0].id}
     *
     * @return the message ID, or {@code null} if not present
     */
    public String extractMessageId(WhatsAppWebhookPayload payload) {
        try {
            List<WhatsAppWebhookPayload.Message> messages = getMessages(payload);
            if (messages == null || messages.isEmpty()) {
                return null;
            }
            return messages.get(0).getId();
        } catch (Exception e) {
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private List<WhatsAppWebhookPayload.Message> getMessages(WhatsAppWebhookPayload payload) {
        if (payload.getEntry() == null || payload.getEntry().isEmpty()) {
            return null;
        }
        List<WhatsAppWebhookPayload.Change> changes = payload.getEntry().get(0).getChanges();
        if (changes == null || changes.isEmpty()) {
            return null;
        }
        WhatsAppWebhookPayload.Value value = changes.get(0).getValue();
        return value != null ? value.getMessages() : null;
    }

    private TenantConfig toTenantConfig(Tenant tenant) {
        return new TenantConfig(
                tenant.getId(),
                tenant.getBusinessName(),
                tenant.getWhatsappPhoneNumberId(),
                tenant.getCalendarApiKey(),
                tenant.getCustomizedSystemPrompt(),
                tenant.getTimezone(),
                tenant.getLocale()
        );
    }
}
