package com.terminai.bookingassistant.repository;

import com.terminai.bookingassistant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Tenant} entities.
 *
 * <p>The primary lookup method is {@link #findByWhatsappPhoneNumberIdAndActiveTrue},
 * which drives the multi-tenant routing logic: given the destination phone number ID
 * from the WhatsApp webhook payload, it returns the matching active tenant or empty.
 */
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    /**
     * Finds an active tenant by its Meta WhatsApp Business Phone Number ID.
     *
     * <p>Only tenants with {@code is_active = true} are returned. Inactive tenants
     * are effectively removed from routing without deleting their data.
     *
     * @param whatsappPhoneNumberId the {@code metadata.phone_number_id} from the webhook payload
     * @return an {@link Optional} containing the matching tenant, or empty if none found
     */
    Optional<Tenant> findByWhatsappPhoneNumberIdAndActiveTrue(String whatsappPhoneNumberId);
}
