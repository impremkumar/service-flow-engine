package com.ebtedge.service.flow.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CardStatusMapperTest {

    @ParameterizedTest
    @CsvSource({
            "A, Active",
            "I, Inactive",
            "S, Suspended",
            "C, Closed",
            "P, Pending",
            "E, Expired",
            "B, Blocked",
            "R, Replaced"
    })
    void testToDisplay_validStatusCodes_returnsCorrectDisplay(String statusCode, String expectedDisplay) {
        // When
        String display = CardStatusMapper.toDisplay(statusCode);

        // Then
        assertEquals(expectedDisplay, display);
    }

    @Test
    void testToDisplay_lowercaseStatusCode_returnsCorrectDisplay() {
        // When
        String display = CardStatusMapper.toDisplay("a");

        // Then
        assertEquals("Active", display);
    }

    @Test
    void testToDisplay_unknownStatusCode_returnsUnknown() {
        // When
        String display = CardStatusMapper.toDisplay("X");

        // Then
        assertEquals("Unknown", display);
    }

    @Test
    void testToDisplay_nullStatusCode_returnsUnknown() {
        // When
        String display = CardStatusMapper.toDisplay(null);

        // Then
        assertEquals("Unknown", display);
    }

    @Test
    void testToDisplay_emptyStatusCode_returnsUnknown() {
        // When
        String display = CardStatusMapper.toDisplay("");

        // Then
        assertEquals("Unknown", display);
    }

    @ParameterizedTest
    @CsvSource({
            "A, true",
            "I, true",
            "S, true",
            "X, false",
            "Z, false"
    })
    void testIsValidStatus(String statusCode, boolean expectedValid) {
        // When
        boolean isValid = CardStatusMapper.isValidStatus(statusCode);

        // Then
        assertEquals(expectedValid, isValid);
    }

    @Test
    void testIsValidStatus_nullStatusCode_returnsFalse() {
        // When
        boolean isValid = CardStatusMapper.isValidStatus(null);

        // Then
        assertFalse(isValid);
    }
}
