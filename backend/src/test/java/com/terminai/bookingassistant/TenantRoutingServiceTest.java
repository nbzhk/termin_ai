package com.terminai.bookingassistant.service;

import com.terminai.bookingassistant.dto.TenantConfig;
import com.terminai.bookingassistant.dto.WhatsAppWebhookPayload;
import com.terminai.bookingassistant.entity.Tenant;
import com.terminai.bookingassistant.exception.TenantNotFoundException;
import com.terminai.bookingassistant.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TenantRoutingService}.
 *
 * <p>Uses Mockito to stub the repository, so no database connection is required.
 */
class TenantRoutingServiceTest {

    private TenantRepository tenantRepository;
    private TenantRoutingService service;

    @BeforeEach
    void setUp() {
        tenantRepository = mock(TenantRepository.class);
        service = new TenantRoutingService(tenantRepository);
    }

    // -------------------------------------------------------------------------
    // resolveTenant
    // -------------------------------------------------------------------------

    @Test
    void resolveTenant_returnsTenantConfig_whenTenantFound() {
        // arrange
        String phoneNumberId = "12345678";
        WhatsAppWebhookPayload payload = buildPayload(phoneNumberId, "49151000000", "wamid.1", "Hello");

        Tenant tenant = buildTenant(phoneNumberId);
        when(tenantRepository.findByWhatsappPhoneNumberIdAndActiveTrue(phoneNumberId))
                .thenReturn(Optional.of(tenant));

        // act
        TenantConfig config = service.resolveTenant(payload);

        // assert
        assertThat(config.getWhatsappPhoneNumberId()).isEqualTo(phoneNumberId);
        assertThat(config.getBusinessName()).isEqualTo("Test Salon");
        assertThat(config.getCustomizedSystemPrompt()).isEqualTo("You are a helpful assistant.");
    }

    @Test
    void resolveTenant_throwsTenantNotFoundException_whenNoTenantFound() {
        // arrange
        String phoneNumberId = "unknown_id";
        WhatsAppWebhookPayload payload = buildPayload(phoneNumberId, "49151000000", "wamid.2", "Hi");

        when(tenantRepository.findByWhatsappPhoneNumberIdAndActiveTrue(phoneNumberId))
                .thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> service.resolveTenant(payload))
                .isInstanceOf(TenantNotFoundException.class)
                .hasMessageContaining(phoneNumberId);
    }

    // -------------------------------------------------------------------------
    // extractPhoneNumberId
    // -------------------------------------------------------------------------

    @Test
    void extractPhoneNumberId_returnsId_forValidPayload() {
        WhatsAppWebhookPayload payload = buildPayload("99887766", "49151000001", "wamid.3", "Test");
        assertThat(service.extractPhoneNumberId(payload)).isEqualTo("99887766");
    }

    @Test
    void extractPhoneNumberId_throwsIllegalArgument_whenMetadataNull() {
        WhatsAppWebhookPayload payload = buildPayloadWithNullMetadata();
        assertThatThrownBy(() -> service.extractPhoneNumberId(payload))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void extractPhoneNumberId_throwsIllegalArgument_whenNoEntries() {
        WhatsAppWebhookPayload payload = new WhatsAppWebhookPayload();
        // entry is null by default
        assertThatThrownBy(() -> service.extractPhoneNumberId(payload))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // -------------------------------------------------------------------------
    // extractCustomerPhoneNumber
    // -------------------------------------------------------------------------

    @Test
    void extractCustomerPhoneNumber_returnsFrom_whenMessagePresent() {
        WhatsAppWebhookPayload payload = buildPayload("11111", "49151999999", "wamid.4", "Hello");
        assertThat(service.extractCustomerPhoneNumber(payload)).isEqualTo("49151999999");
    }

    @Test
    void extractCustomerPhoneNumber_returnsNull_whenNoMessages() {
        WhatsAppWebhookPayload payload = buildPayloadNoMessages("11111");
        assertThat(service.extractCustomerPhoneNumber(payload)).isNull();
    }

    // -------------------------------------------------------------------------
    // extractMessageText
    // -------------------------------------------------------------------------

    @Test
    void extractMessageText_returnsBody_whenTextMessagePresent() {
        WhatsAppWebhookPayload payload = buildPayload("11111", "49151999999", "wamid.5", "Book me");
        assertThat(service.extractMessageText(payload)).isEqualTo("Book me");
    }

    // -------------------------------------------------------------------------
    // extractMessageId
    // -------------------------------------------------------------------------

    @Test
    void extractMessageId_returnsWamid_whenPresent() {
        WhatsAppWebhookPayload payload = buildPayload("11111", "49151999999", "wamid.6", "Text");
        assertThat(service.extractMessageId(payload)).isEqualTo("wamid.6");
    }

    // -------------------------------------------------------------------------
    // Builder helpers
    // -------------------------------------------------------------------------

    private static WhatsAppWebhookPayload buildPayload(
            String phoneNumberId,
            String fromNumber,
            String messageId,
            String messageBody
    ) {
        // Use Jackson deserialization would be cleaner, but constructor approach avoids
        // Jackson dependency in unit test and validates the extraction paths directly.
        WhatsAppWebhookPayload.TextContent text = new WhatsAppWebhookPayload.TextContent();
        setField(text, "body", messageBody);

        WhatsAppWebhookPayload.Message message = new WhatsAppWebhookPayload.Message();
        setField(message, "id", messageId);
        setField(message, "from", fromNumber);
        setField(message, "type", "text");
        setField(message, "text", text);

        WhatsAppWebhookPayload.Metadata metadata = new WhatsAppWebhookPayload.Metadata();
        setField(metadata, "phoneNumberId", phoneNumberId);

        WhatsAppWebhookPayload.Value value = new WhatsAppWebhookPayload.Value();
        setField(value, "metadata", metadata);
        setField(value, "messages", List.of(message));

        WhatsAppWebhookPayload.Change change = new WhatsAppWebhookPayload.Change();
        setField(change, "value", value);
        setField(change, "field", "messages");

        WhatsAppWebhookPayload.Entry entry = new WhatsAppWebhookPayload.Entry();
        setField(entry, "id", "waba_id");
        setField(entry, "changes", List.of(change));

        WhatsAppWebhookPayload payload = new WhatsAppWebhookPayload();
        setField(payload, "object", "whatsapp_business_account");
        setField(payload, "entry", List.of(entry));

        return payload;
    }

    private static WhatsAppWebhookPayload buildPayloadNoMessages(String phoneNumberId) {
        WhatsAppWebhookPayload.Metadata metadata = new WhatsAppWebhookPayload.Metadata();
        setField(metadata, "phoneNumberId", phoneNumberId);

        WhatsAppWebhookPayload.Value value = new WhatsAppWebhookPayload.Value();
        setField(value, "metadata", metadata);
        setField(value, "messages", List.of());

        WhatsAppWebhookPayload.Change change = new WhatsAppWebhookPayload.Change();
        setField(change, "value", value);

        WhatsAppWebhookPayload.Entry entry = new WhatsAppWebhookPayload.Entry();
        setField(entry, "changes", List.of(change));

        WhatsAppWebhookPayload payload = new WhatsAppWebhookPayload();
        setField(payload, "entry", List.of(entry));

        return payload;
    }

    private static WhatsAppWebhookPayload buildPayloadWithNullMetadata() {
        WhatsAppWebhookPayload.Value value = new WhatsAppWebhookPayload.Value();
        // metadata intentionally left null

        WhatsAppWebhookPayload.Change change = new WhatsAppWebhookPayload.Change();
        setField(change, "value", value);

        WhatsAppWebhookPayload.Entry entry = new WhatsAppWebhookPayload.Entry();
        setField(entry, "changes", List.of(change));

        WhatsAppWebhookPayload payload = new WhatsAppWebhookPayload();
        setField(payload, "entry", List.of(entry));

        return payload;
    }

    private static Tenant buildTenant(String phoneNumberId) {
        Tenant t = new Tenant();
        t.setBusinessName("Test Salon");
        t.setWhatsappPhoneNumberId(phoneNumberId);
        t.setCustomizedSystemPrompt("You are a helpful assistant.");
        t.setTimezone("UTC");
        t.setLocale("en");
        t.setActive(true);
        return t;
    }

    /** Reflective field setter to avoid adding public setters not needed in prod. */
    private static void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }

    private static java.lang.reflect.Field findField(Class<?> clazz, String name) {
        Class<?> current = clazz;
        while (current != null) {
            for (java.lang.reflect.Field f : current.getDeclaredFields()) {
                if (f.getName().equals(name)) return f;
            }
            current = current.getSuperclass();
        }
        throw new RuntimeException("Field not found: " + name + " in " + clazz);
    }
}
