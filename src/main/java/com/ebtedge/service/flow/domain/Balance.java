package com.ebtedge.service.flow.domain;

import com.ebtedge.service.flow.event.annotation.KafkaField;

/**
 * Balance information for a client.
 * Fields annotated with @KafkaField will be automatically included in Kafka events.
 */
public record Balance(
    @KafkaField String clientId,
    @KafkaField double amount
) {
}
