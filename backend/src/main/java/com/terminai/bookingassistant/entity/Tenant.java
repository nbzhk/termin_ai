package com.terminai.bookingassistant.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity representing a single salon/business (tenant) using the assistant.
 *
 * <p>Each tenant is identified by its {@code whatsappPhoneNumberId}, which matches
 * the {@code metadata.phone_number_id} field in the Meta WhatsApp Cloud API webhook payload.
 * This is the canonical routing key used to resolve the correct tenant configuration
 * for every inbound message.
 *
 * <p>The {@code customizedSystemPrompt} field is injected into the LLM system prompt
 * at inference time, allowing each salon to define its own assistant persona, services,
 * and business hours without code changes.
 */
@Entity
@Table(
    name = "tenants",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_tenants_whatsapp_phone_number_id",
        columnNames = "whatsapp_phone_number_id"
    )
)
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Human-readable business/salon name. */
    @Column(name = "business_name", nullable = false)
    private String businessName;

    /**
     * Meta WhatsApp Business Phone Number ID.
     * Found at {@code entry[0].changes[0].value.metadata.phone_number_id} in the webhook payload.
     */
    @Column(name = "whatsapp_phone_number_id", nullable = false, unique = true)
    private String whatsappPhoneNumberId;

    /** Optional E.164 display number (e.g. "+49151..."). */
    @Column(name = "whatsapp_display_number")
    private String whatsappDisplayNumber;

    /**
     * External calendar API credential.
     * Store a secret reference (not raw key) in production environments.
     */
    @Column(name = "calendar_api_key")
    private String calendarApiKey;

    /**
     * Tenant-specific LLM system prompt.
     * Defines the assistant persona, services, business hours, tone, etc.
     */
    @Column(name = "customized_system_prompt", nullable = false, columnDefinition = "TEXT")
    private String customizedSystemPrompt = "";

    /** IANA timezone string, e.g. "Europe/Berlin". */
    @Column(name = "timezone", nullable = false)
    private String timezone = "UTC";

    /** BCP 47 language tag, e.g. "en", "de". */
    @Column(name = "locale", nullable = false)
    private String locale = "en";

    /** Soft-delete flag. Inactive tenants are excluded from routing. */
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public UUID getId() {
        return id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getWhatsappPhoneNumberId() {
        return whatsappPhoneNumberId;
    }

    public void setWhatsappPhoneNumberId(String whatsappPhoneNumberId) {
        this.whatsappPhoneNumberId = whatsappPhoneNumberId;
    }

    public String getWhatsappDisplayNumber() {
        return whatsappDisplayNumber;
    }

    public void setWhatsappDisplayNumber(String whatsappDisplayNumber) {
        this.whatsappDisplayNumber = whatsappDisplayNumber;
    }

    public String getCalendarApiKey() {
        return calendarApiKey;
    }

    public void setCalendarApiKey(String calendarApiKey) {
        this.calendarApiKey = calendarApiKey;
    }

    public String getCustomizedSystemPrompt() {
        return customizedSystemPrompt;
    }

    public void setCustomizedSystemPrompt(String customizedSystemPrompt) {
        this.customizedSystemPrompt = customizedSystemPrompt;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
