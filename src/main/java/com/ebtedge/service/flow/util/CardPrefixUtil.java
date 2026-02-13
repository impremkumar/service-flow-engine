package com.ebtedge.service.flow.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for extracting card prefix from card numbers.
 * Card prefix is defined as the first 6 digits of a 16-digit card number.
 */
@Slf4j
public final class CardPrefixUtil {

    private static final int CARD_PREFIX_LENGTH = 6;
    private static final int EXPECTED_CARD_LENGTH = 16;

    private CardPrefixUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Extracts the card prefix (first 6 digits) from a card number.
     *
     * @param cardNumber The 16-digit card number
     * @return The card prefix (first 6 digits)
     * @throws IllegalArgumentException if card number is invalid
     */
    public static String extractPrefix(String cardNumber) {
        if (cardNumber == null) {
            log.error("Card number is null");
            throw new IllegalArgumentException("Card number cannot be null");
        }

        // Remove any spaces or hyphens
        String cleanedCardNumber = cardNumber.replaceAll("[\\s-]", "");

        if (cleanedCardNumber.length() != EXPECTED_CARD_LENGTH) {
            log.warn("Invalid card number length: expected {}, got {} for card: {}",
                    EXPECTED_CARD_LENGTH, cleanedCardNumber.length(), maskCardNumber(cardNumber));
            throw new IllegalArgumentException(
                String.format("Card number must be %d digits, got %d",
                    EXPECTED_CARD_LENGTH, cleanedCardNumber.length())
            );
        }

        if (!cleanedCardNumber.matches("\\d+")) {
            log.error("Card number contains non-digit characters: {}", maskCardNumber(cardNumber));
            throw new IllegalArgumentException("Card number must contain only digits");
        }

        String prefix = cleanedCardNumber.substring(0, CARD_PREFIX_LENGTH);
        log.debug("Extracted card prefix: {} from card: {}", prefix, maskCardNumber(cardNumber));
        return prefix;
    }

    /**
     * Masks a card number for safe logging (shows only first 6 and last 4 digits).
     *
     * @param cardNumber The card number to mask
     * @return Masked card number (e.g., "123456******7890")
     */
    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 10) {
            return "****";
        }
        String cleaned = cardNumber.replaceAll("[\\s-]", "");
        if (cleaned.length() == EXPECTED_CARD_LENGTH) {
            return cleaned.substring(0, 6) + "******" + cleaned.substring(12);
        }
        return cleaned.substring(0, Math.min(4, cleaned.length())) + "****";
    }
}
