package com.terminai.bookingassistant.controller;

import com.terminai.bookingassistant.dto.TenantConfig;
import com.terminai.bookingassistant.dto.WhatsAppWebhookPayload;
import com.terminai.bookingassistant.service.WhatsAppWebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller that handles Meta WhatsApp Cloud API webhook events.
 *
 * <p>Exposes two endpoints under {@code /webhook}:
 * <ul>
 *   <li>{@code GET  /webhook} — Meta webhook verification (one-time setup handshake)</li>
 *   <li>{@code POST /webhook} — Inbound WhatsApp messages and status updates</li>
 * </ul>
 *
 * <p>The controller intentionally contains minimal logic. All routing and persistence
 * are delegated to {@link WhatsAppWebhookService}.
 */
@RestController
@RequestMapping("/webhook")
public class WhatsAppWebhookController {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    /** Fixed response string expected by Meta for all successful POST webhook deliveries. */
    private static final String META_ACKNOWLEDGEMENT = "EVENT_RECEIVED";

    private final WhatsAppWebhookService webhookService;

    /**
     * Verification token configured in the Meta Developer Console.
     * Must match the value set under App → Webhooks → Edit → Verify Token.
     * Store this value in an environment variable; never commit it to source control.
     */
    @Value("${meta.webhook.verify-token}")
    private String verifyToken;

    public WhatsAppWebhookController(WhatsAppWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    // -------------------------------------------------------------------------
    // GET /webhook — Meta verification handshake
    // -------------------------------------------------------------------------

    /**
     * Meta webhook verification endpoint.
     *
     * <p>When a webhook subscription is created or updated in the Meta Developer Console,
     * Meta sends a GET request with three query parameters:
     * <ul>
     *   <li>{@code hub.mode}         — always {@code "subscribe"}</li>
     *   <li>{@code hub.verify_token} — the token you configured in the console</li>
     *   <li>{@code hub.challenge}    — a random string that must be echoed back</li>
     * </ul>
     *
     * <p>If the token matches, respond with the challenge value and HTTP 200.
     * Meta will then mark the webhook as verified.
     *
     * @param mode      the hub mode ({@code subscribe})
     * @param token     the verify token sent by Meta
     * @param challenge the challenge string to echo back
     * @return the challenge string on success, or 403 if verification fails
     */
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode",         required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge",    required = false) String challenge
    ) {
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("Meta webhook verification successful.");
            return ResponseEntity.ok(challenge);
        }
        log.warn("Meta webhook verification failed: mode={}, token matches={}", mode, verifyToken.equals(token));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verification failed");
    }

    // -------------------------------------------------------------------------
    // POST /webhook — Inbound messages and status updates
    // -------------------------------------------------------------------------

    /**
     * Incoming WhatsApp message endpoint.
     *
     * <p>Meta delivers all inbound messages and status updates here.
     * The response must be HTTP 200 with body {@code "EVENT_RECEIVED"} within 20 seconds;
     * otherwise Meta will retry the delivery.
     *
     * <p>Processing steps:
     * <ol>
     *   <li>Parse the typed {@link WhatsAppWebhookPayload}</li>
     *   <li>Resolve the tenant via {@link WhatsAppWebhookService#resolveTenantFromPayload}</li>
     *   <li>Persist and (later) process the message via {@link WhatsAppWebhookService#handleInboundMessage}</li>
     *   <li>Acknowledge immediately with 200 / {@code "EVENT_RECEIVED"}</li>
     * </ol>
     *
     * @param payload the deserialized WhatsApp webhook payload
     * @return {@code 200 EVENT_RECEIVED} always (errors are logged, not surfaced to Meta)
     */
    @PostMapping
    public ResponseEntity<String> receiveMessage(@RequestBody WhatsAppWebhookPayload payload) {
        try {
            TenantConfig tenantConfig = webhookService.resolveTenantFromPayload(payload);
            webhookService.handleInboundMessage(payload, tenantConfig);
        } catch (Exception e) {
            // Always return 200 to Meta to prevent duplicate delivery retries.
            // Internal errors are logged for investigation.
            log.error("Error processing inbound WhatsApp webhook: {}", e.getMessage(), e);
        }
        return ResponseEntity.ok(META_ACKNOWLEDGEMENT);
    }
}
