package com.ebtedge.service.flow.domain;

import com.ebtedge.service.flow.event.annotation.KafkaField;

/**
 * UI response containing balance and demographics information.
 * Uses nested=true to automatically extract @KafkaField annotated fields from nested objects.
 */
public record UIResponse(
    @KafkaField(nested = true) Balance balance,
    @KafkaField(nested = true) Demographics demographics
) {
}
