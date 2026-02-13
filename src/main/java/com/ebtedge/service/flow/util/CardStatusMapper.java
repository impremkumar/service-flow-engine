package com.ebtedge.service.flow.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Utility class for mapping card status codes to human-readable display values.
 * Provides centralized status mapping for consistency across the application.
 */
@Slf4j
public final class CardStatusMapper {

    private static final Map<String, String> STATUS_MAP = Map.of(
        "A", "Active",
        "I", "Inactive",
        "S", "Suspended",
        "C", "Closed",
        "P", "Pending",
        "E", "Expired",
        "B", "Blocked",
        "R", "Replaced"
    );

    private static final String UNKNOWN_STATUS = "Unknown";

    private CardStatusMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Maps a card status code to its display value.
     *
     * @param statusCode The card status code (e.g., "A", "I", "S")
     * @return The display value (e.g., "Active", "Inactive", "Suspended")
     */
    public static String toDisplay(String statusCode) {
        if (statusCode == null || statusCode.trim().isEmpty()) {
            log.warn("Null or empty status code provided");
            return UNKNOWN_STATUS;
        }

        String displayValue = STATUS_MAP.getOrDefault(statusCode.toUpperCase(), UNKNOWN_STATUS);

        if (UNKNOWN_STATUS.equals(displayValue)) {
            log.warn("Unknown card status code: {}", statusCode);
        } else {
            log.debug("Mapped status code {} to {}", statusCode, displayValue);
        }

        return displayValue;
    }

    /**
     * Checks if a given status code is valid.
     *
     * @param statusCode The status code to validate
     * @return true if the status code is recognized, false otherwise
     */
    public static boolean isValidStatus(String statusCode) {
        return statusCode != null && STATUS_MAP.containsKey(statusCode.toUpperCase());
    }
}
