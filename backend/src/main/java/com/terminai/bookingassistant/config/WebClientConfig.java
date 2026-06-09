package com.terminai.bookingassistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * HTTP client configuration.
 *
 * <p>Provides a shared {@link WebClient} instance for outbound API calls:
 * <ul>
 *   <li>Meta Cloud API — sending WhatsApp messages via the Send API</li>
 *   <li>Gemini API     — LLM inference calls (placeholder, not yet implemented)</li>
 * </ul>
 */
@Configuration
public class WebClientConfig {

    /**
     * General-purpose {@link WebClient} bean.
     *
     * <p>Downstream services should inject this bean and set the appropriate
     * base URL / headers per request. Example:
     * <pre>{@code
     * webClient.post()
     *          .uri("https://graph.facebook.com/v19.0/{phoneNumberId}/messages", phoneNumberId)
     *          .header(HttpHeaders.AUTHORIZATION, "Bearer " + metaAccessToken)
     *          .bodyValue(requestBody)
     *          .retrieve()
     *          .bodyToMono(String.class)
     *          .block();
     * }</pre>
     */
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
