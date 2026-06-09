package com.terminai.bookingassistant.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity representing a single message in the conversation history.
 *
 * <p>Records are partitioned by {@code tenant_id} and {@code customerPhoneNumber},
 * ensuring strict data isolation between tenants. The {@code role} field follows the
 * OpenAI / Gemini conversation format ({@code user}, {@code assistant}, {@code system}),
 * making it straightforward to reconstruct a context window for LLM calls.
 */
@Entity
@Table(
    name = "chat_history",
    indexes = {
        @Index(
            name = "idx_chat_history_tenant_customer",
            columnList = "tenant_id, customer_phone_number, created_at DESC"
        ),
        @Index(
            name = "idx_chat_history_whatsapp_message_id",
            columnList = "whatsapp_message_id"
        )
    }
)
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Owning tenant. Loaded lazily to avoid N+1 queries when only the
     * foreign key value is needed.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    /** Customer's WhatsApp number in E.164 format. */
    @Column(name = "customer_phone_number", nullable = false)
    private String customerPhoneNumber;

    /** Meta-assigned message ID (wamid). Used for deduplication. */
    @Column(name = "whatsapp_message_id")
    private String whatsappMessageId;

    /**
     * Message direction:
     * <ul>
     *   <li>{@code inbound}  – customer → business (received via webhook)</li>
     *   <li>{@code outbound} – business/assistant → customer</li>
     * </ul>
     */
    @Column(name = "direction", nullable = false)
    private String direction;

    /**
     * LLM conversation role:
     * <ul>
     *   <li>{@code user}      – end customer message</li>
     *   <li>{@code assistant} – AI-generated response</li>
     *   <li>{@code system}    – system prompt or internal note</li>
     * </ul>
     */
    @Column(name = "role", nullable = false)
    private String role;

    /** Plain text content of the message. */
    @Column(name = "message_text", columnDefinition = "TEXT")
    private String messageText;

    /**
     * Full raw webhook payload serialized to JSON.
     * Stored for auditability and debugging.
     * Future: upgrade column type to JSONB for native PostgreSQL querying.
     */
    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    public void setCustomerPhoneNumber(String customerPhoneNumber) {
        this.customerPhoneNumber = customerPhoneNumber;
    }

    public String getWhatsappMessageId() {
        return whatsappMessageId;
    }

    public void setWhatsappMessageId(String whatsappMessageId) {
        this.whatsappMessageId = whatsappMessageId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
