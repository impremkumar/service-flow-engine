package com.ebtedge.service.flow.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardholderIdEncryptionUtilTest {

    @Test
    void testEncrypt_validInputs_success() {
        // Given
        String agency = "AG001";
        String clientId = "CLI123456";

        // When
        String encrypted = CardholderIdEncryptionUtil.encrypt(agency, clientId);

        // Then
        assertNotNull(encrypted);
        assertFalse(encrypted.isEmpty());
        assertTrue(encrypted.length() > 0);
    }

    @Test
    void testEncrypt_andDecrypt_roundTrip() {
        // Given
        String agency = "AG001";
        String clientId = "CLI123456";

        // When
        String encrypted = CardholderIdEncryptionUtil.encrypt(agency, clientId);
        String[] decrypted = CardholderIdEncryptionUtil.decrypt(encrypted);

        // Then
        assertEquals(2, decrypted.length);
        assertEquals(agency, decrypted[0]);
        assertEquals(clientId, decrypted[1]);
    }

    @Test
    void testEncrypt_nullAgency_throwsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class,
                () -> CardholderIdEncryptionUtil.encrypt(null, "CLI123456"));
    }

    @Test
    void testEncrypt_nullClientId_throwsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class,
                () -> CardholderIdEncryptionUtil.encrypt("AG001", null));
    }

    @Test
    void testDecrypt_nullEncryptedId_throwsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class,
                () -> CardholderIdEncryptionUtil.decrypt(null));
    }

    @Test
    void testDecrypt_invalidFormat_throwsException() {
        // When/Then
        assertThrows(IllegalArgumentException.class,
                () -> CardholderIdEncryptionUtil.decrypt("invalid!!!"));
    }

    @Test
    void testEncrypt_differentInputs_differentOutputs() {
        // Given
        String encrypted1 = CardholderIdEncryptionUtil.encrypt("AG001", "CLI001");
        String encrypted2 = CardholderIdEncryptionUtil.encrypt("AG002", "CLI001");

        // Then
        assertNotEquals(encrypted1, encrypted2);
    }
}
