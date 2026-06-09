package com.terminai.bookingassistant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Typed representation of the Meta WhatsApp Cloud API webhook payload.
 *
 * <p>Only the fields required for routing and message persistence are mapped here.
 * Unknown fields are ignored ({@code @JsonIgnoreProperties(ignoreUnknown = true)})
 * to keep the DTO resilient against API version changes.
 *
 * <p>Example incoming payload structure:
 * <pre>{@code
 * {
 *   "object": "whatsapp_business_account",
 *   "entry": [{
 *     "id": "<WHATSAPP_BUSINESS_ACCOUNT_ID>",
 *     "changes": [{
 *       "value": {
 *         "metadata": { "phone_number_id": "...", "display_phone_number": "..." },
 *         "messages": [{ "id": "wamid...", "from": "49151...", "text": {"body": "Hello"} }]
 *       },
 *       "field": "messages"
 *     }]
 *   }]
 * }
 * }</pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppWebhookPayload {

    @JsonProperty("object")
    private String object;

    @JsonProperty("entry")
    private List<Entry> entry;

    // -------------------------------------------------------------------------
    // Nested classes
    // -------------------------------------------------------------------------

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {
        @JsonProperty("id")
        private String id;

        @JsonProperty("changes")
        private List<Change> changes;

        public String getId() { return id; }
        public List<Change> getChanges() { return changes; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Change {
        @JsonProperty("value")
        private Value value;

        @JsonProperty("field")
        private String field;

        public Value getValue() { return value; }
        public String getField() { return field; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        @JsonProperty("metadata")
        private Metadata metadata;

        @JsonProperty("messages")
        private List<Message> messages;

        public Metadata getMetadata() { return metadata; }
        public List<Message> getMessages() { return messages; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        @JsonProperty("phone_number_id")
        private String phoneNumberId;

        @JsonProperty("display_phone_number")
        private String displayPhoneNumber;

        public String getPhoneNumberId() { return phoneNumberId; }
        public String getDisplayPhoneNumber() { return displayPhoneNumber; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        @JsonProperty("id")
        private String id;

        @JsonProperty("from")
        private String from;

        @JsonProperty("type")
        private String type;

        @JsonProperty("text")
        private TextContent text;

        public String getId() { return id; }
        public String getFrom() { return from; }
        public String getType() { return type; }
        public TextContent getText() { return text; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextContent {
        @JsonProperty("body")
        private String body;

        public String getBody() { return body; }
    }

    // -------------------------------------------------------------------------
    // Root-level getters
    // -------------------------------------------------------------------------

    public String getObject() { return object; }
    public List<Entry> getEntry() { return entry; }
}
