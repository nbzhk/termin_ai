package com.terminai.bookingassistant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test that verifies the Spring application context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class BookingAssistantApplicationTests {

    @Test
    void contextLoads() {
        // If the context fails to start, this test will fail with a descriptive error.
    }
}
