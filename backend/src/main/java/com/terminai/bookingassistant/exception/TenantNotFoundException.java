package com.terminai.bookingassistant.exception;

/**
 * Thrown when no active tenant can be found for a given WhatsApp Phone Number ID.
 *
 * <p>This typically means the incoming message was delivered to a phone number that
 * is not registered in the system, or the matching tenant has been deactivated.
 */
public class TenantNotFoundException extends RuntimeException {

    public TenantNotFoundException(String message) {
        super(message);
    }

    public TenantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
