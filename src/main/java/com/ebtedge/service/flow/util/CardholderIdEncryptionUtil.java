package com.ebtedge.service.flow.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility class for encrypting and encoding cardholder identifiers.
 * This creates an encrypted cardholderId from agency code and client ID.
 *
 * Note: This uses Base64 encoding as a simple implementation.
 * In production, replace with proper encryption (AES-256, etc.)
 */
@Slf4j
public final class CardholderIdEncryptionUtil {

    private CardholderIdEncryptionUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Encrypts and encodes the combination of agency and clientId.
     * Format: Base64(agency:clientId)
     *
     * @param agency The agency code
     * @param clientId The client identifier
     * @return Encrypted and encoded cardholder ID
     */
    public static String encrypt(String agency, String clientId) {
        if (agency == null || clientId == null) {
            log.warn("Null values provided for encryption: agency={}, clientId={}", agency, clientId);
            throw new IllegalArgumentException("Agency and clientId cannot be null");
        }

        String combined = agency + ":" + clientId;
        String encoded = Base64.getEncoder().encodeToString(combined.getBytes(StandardCharsets.UTF_8));

        log.debug("Encrypted cardholderId for agency={}, clientId={}", agency, clientId);
        return encoded;
    }

    /**
     * Decrypts and decodes a cardholder ID back to agency and clientId.
     * This is useful for verification and testing purposes.
     *
     * @param encryptedId The encrypted cardholder ID
     * @return Array with [agency, clientId]
     */
    public static String[] decrypt(String encryptedId) {
        if (encryptedId == null) {
            throw new IllegalArgumentException("Encrypted ID cannot be null");
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedId);
            String combined = new String(decodedBytes, StandardCharsets.UTF_8);
            return combined.split(":", 2);
        } catch (IllegalArgumentException e) {
            log.error("Failed to decrypt cardholderId: {}", encryptedId, e);
            throw new IllegalArgumentException("Invalid encrypted ID format", e);
        }
    }
}
