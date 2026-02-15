package com.ebtedge.service.flow.domain;

import com.ebtedge.service.flow.event.annotation.KafkaField;

/**
 * Demographic information for a client.
 * Fields annotated with @KafkaField will be automatically included in Kafka events.
 * Note: email is deliberately NOT annotated to exclude it from events (privacy).
 */
public record Demographics(
    @KafkaField String name,
    String email,  // NOT included in Kafka events (privacy/security)
    @KafkaField String city
) {
}
