package com.ebtedge.service.flow.domain;

/**
 * Type-safe container for combined profile data.
 * Eliminates need for Map<String, Object> and unsafe casting.
 */
public record ProfileData(Balance balance, Demographics demographics) {
}
