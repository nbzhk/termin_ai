package com.terminai.bookingassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the multi-tenant WhatsApp AI Booking Assistant backend.
 *
 * <p>This service handles:
 * <ul>
 *   <li>Meta WhatsApp webhook verification and incoming message routing</li>
 *   <li>Multi-tenant resolution based on destination WhatsApp Phone Number ID</li>
 *   <li>Conversation history persistence per tenant and customer</li>
 * </ul>
 *
 * <p>LLM (Gemini) and calendar integration are scaffolded but not yet implemented.
 */
@SpringBootApplication
public class BookingAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingAssistantApplication.class, args);
    }
}
