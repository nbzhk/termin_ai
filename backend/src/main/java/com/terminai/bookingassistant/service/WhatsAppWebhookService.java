package com.terminai.bookingassistant.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.terminai.bookingassistant.dto.TenantConfig;
import com.terminai.bookingassistant.dto.WhatsAppWebhookPayload;
import com.terminai.bookingassistant.entity.ChatHistory;
import com.terminai.bookingassistant.entity.Tenant;
import com.terminai.bookingassistant.repository.ChatHistoryRepository;
import com.terminai.bookingassistant.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration service for inbound WhatsApp webhook events.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Delegate tenant resolution to {@link TenantRoutingService}</li>
 *   <li>Persist inbound messages to {@code chat_history}</li>
 *   <li>
 *     (Future) Pass the resolved {@link TenantConfig} and recent conversation history
 *     to the Gemini LLM service and send the generated response back via Meta Send API
 *   </li>
 * </ol>
 *
 * <p><b>LLM integration placeholder:</b> see {@link #processMessage} for where Gemini
 * calls will be wired in once the LLM layer is implemented.
 */
@Service
public class WhatsAppWebhookService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookService.class);

    private final TenantRoutingService tenantRoutingService;
    private final TenantRepository tenantRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final ObjectMapper objectMapper;

    public WhatsAppWebhookService(
            TenantRoutingService tenantRoutingService,
            TenantRepository tenantRepository,
            ChatHistoryRepository chatHistoryRepository,
            ObjectMapper objectMapper
    ) {
        this.tenantRoutingService = tenantRoutingService;
        this.tenantRepository = tenantRepository;
        this.chatHistoryRepository = chatHistoryRepository;
        this.objectMapper = objectMapper;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Resolves the tenant from the inbound webhook payload.
     *
     * @param payload the parsed WhatsApp webhook payload
     * @return the matching tenant's configuration
     */
    public TenantConfig resolveTenantFromPayload(WhatsAppWebhookPayload payload) {
        return tenantRoutingService.resolveTenant(payload);
    }

    /**
     * Persists an inbound message and kicks off downstream processing.
     *
     * <p>The method:
     * <ol>
     *   <li>Saves the raw payload and extracted fields to {@code chat_history}</li>
     *   <li>
     *     Calls {@link #processMessage} which is a placeholder for the LLM pipeline
     *   </li>
     * </ol>
     *
     * @param payload      the parsed WhatsApp webhook payload
     * @param tenantConfig the resolved tenant configuration
     */
    @Transactional
    public void handleInboundMessage(WhatsAppWebhookPayload payload, TenantConfig tenantConfig) {
        String customerPhone = tenantRoutingService.extractCustomerPhoneNumber(payload);
        String messageText   = tenantRoutingService.extractMessageText(payload);
        String messageId     = tenantRoutingService.extractMessageId(payload);

        if (customerPhone == null) {
            // Status update or non-message event — nothing to persist or process
            log.debug("Received webhook event with no customer message; skipping persistence.");
            return;
        }

        Tenant tenant = tenantRepository.findById(tenantConfig.getTenantId())
                .orElseThrow(() -> new IllegalStateException(
                        "Tenant disappeared after routing: " + tenantConfig.getTenantId()
                ));

        ChatHistory record = new ChatHistory();
        record.setTenant(tenant);
        record.setCustomerPhoneNumber(customerPhone);
        record.setWhatsappMessageId(messageId);
        record.setDirection("inbound");
        record.setRole("user");
        record.setMessageText(messageText);
        record.setRawPayload(serialize(payload));

        chatHistoryRepository.save(record);
        log.info("Saved inbound message [wamid={}] for tenant [{}]",
                messageId, tenantConfig.getBusinessName());

        // -----------------------------------------------------------------------
        // TODO: LLM pipeline (Gemini integration — not yet implemented)
        // -----------------------------------------------------------------------
        processMessage(tenantConfig, customerPhone, messageText);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Placeholder for the Gemini LLM pipeline.
     *
     * <p>When implemented, this method should:
     * <ol>
     *   <li>Load recent {@code chat_history} rows for (tenant, customerPhone)</li>
     *   <li>Build a Gemini request with the tenant's {@code customizedSystemPrompt}
     *       and the conversation context window</li>
     *   <li>Call the Gemini API and receive the assistant response</li>
     *   <li>Persist the response as an {@code outbound} / {@code assistant} chat history record</li>
     *   <li>Send the response back to the customer via the Meta Cloud API Send endpoint</li>
     * </ol>
     *
     * @param tenantConfig   resolved tenant configuration (contains system prompt, locale, etc.)
     * @param customerPhone  customer's WhatsApp number (routing target for the reply)
     * @param inboundMessage the customer's raw message text
     */
    private void processMessage(TenantConfig tenantConfig, String customerPhone, String inboundMessage) {
        // LLM integration not yet implemented.
        log.info("processMessage called for tenant [{}], customer [{}] — LLM pipeline pending.",
                tenantConfig.getBusinessName(), customerPhone);
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize payload to JSON", e);
            return "{}";
        }
    }
}
