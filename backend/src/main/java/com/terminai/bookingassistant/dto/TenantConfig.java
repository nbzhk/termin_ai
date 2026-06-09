package com.terminai.bookingassistant.dto;

import java.util.UUID;

/**
 * Read-only data transfer object returned after multi-tenant routing.
 *
 * <p>Carries the resolved tenant configuration needed by downstream processing:
 * <ul>
 *   <li>The customized system prompt, injected into the LLM at inference time</li>
 *   <li>The calendar API key, used when booking appointments (future)</li>
 *   <li>Locale and timezone, used for natural language date/time formatting</li>
 * </ul>
 *
 * <p>This DTO deliberately omits sensitive or internal fields (e.g. raw DB timestamps)
 * to keep the service layer contracts clean.
 */
public class TenantConfig {

    private final UUID tenantId;
    private final String businessName;
    private final String whatsappPhoneNumberId;
    private final String calendarApiKey;
    private final String customizedSystemPrompt;
    private final String timezone;
    private final String locale;

    public TenantConfig(
            UUID tenantId,
            String businessName,
            String whatsappPhoneNumberId,
            String calendarApiKey,
            String customizedSystemPrompt,
            String timezone,
            String locale
    ) {
        this.tenantId = tenantId;
        this.businessName = businessName;
        this.whatsappPhoneNumberId = whatsappPhoneNumberId;
        this.calendarApiKey = calendarApiKey;
        this.customizedSystemPrompt = customizedSystemPrompt;
        this.timezone = timezone;
        this.locale = locale;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getWhatsappPhoneNumberId() {
        return whatsappPhoneNumberId;
    }

    public String getCalendarApiKey() {
        return calendarApiKey;
    }

    public String getCustomizedSystemPrompt() {
        return customizedSystemPrompt;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getLocale() {
        return locale;
    }
}
