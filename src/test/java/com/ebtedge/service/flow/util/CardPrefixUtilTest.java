package com.ebtedge.service.flow.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardPrefixUtilTest {

    @Test
    void testExtractPrefix_validCardNumber_returnsFirst6Digits() {
        // Given
        String cardNumber = "1234567890123456";

        // When
        String prefix = CardPrefixUtil.extractPrefix(cardNumber);

        // Then
        assertEquals("123456", prefix);
    }

    @Test
    void testExtractPrefix_cardNumberWithSpaces_returnsFirst6Digits() {
        // Given
        String cardNumber = "1234 5678 9012 3456";

        // When
        String prefix = CardPrefixUtil.extractPrefix(cardNumber);

        // Then
        assertEquals("123456", prefix);
    }

    @Test
    void testExtractPrefix_cardNumberWithHyphens_returnsFirst6Digits() {
        // Given
        String cardNumber = "1234-5678-9012-3456";

        // When
        String prefix = CardPrefixUtil.extractPrefix(cardNumber);

        // Then
        assertEquals("123456", prefix);
    }

    @Test
    void testExtractPrefix_nullCardNumber_throwsException() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CardPrefixUtil.extractPrefix(null));

        assertEquals("Card number cannot be null", exception.getMessage());
    }

    @Test
    void testExtractPrefix_shortCardNumber_throwsException() {
        // Given
        String cardNumber = "123456789012345"; // 15 digits

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CardPrefixUtil.extractPrefix(cardNumber));

        assertTrue(exception.getMessage().contains("Card number must be 16 digits"));
    }

    @Test
    void testExtractPrefix_longCardNumber_throwsException() {
        // Given
        String cardNumber = "12345678901234567"; // 17 digits

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CardPrefixUtil.extractPrefix(cardNumber));

        assertTrue(exception.getMessage().contains("Card number must be 16 digits"));
    }

    @Test
    void testExtractPrefix_nonDigitCharacters_throwsException() {
        // Given
        String cardNumber = "123456789012345A";

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CardPrefixUtil.extractPrefix(cardNumber));

        assertEquals("Card number must contain only digits", exception.getMessage());
    }

    @Test
    void testExtractPrefix_differentCardNumbers_differentPrefixes() {
        // Given
        String cardNumber1 = "4111111111111111";
        String cardNumber2 = "5500000000000004";

        // When
        String prefix1 = CardPrefixUtil.extractPrefix(cardNumber1);
        String prefix2 = CardPrefixUtil.extractPrefix(cardNumber2);

        // Then
        assertEquals("411111", prefix1);
        assertEquals("550000", prefix2);
        assertNotEquals(prefix1, prefix2);
    }
}
