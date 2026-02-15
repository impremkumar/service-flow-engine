package com.ebtedge.service.flow.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable record representing metadata for a Kafka event.
 * Contains contextual information about the event for tracing and versioning.
 */
public record KafkaEventMetadata(
    String eventName,
    String topic,
    String schemaVersion,
    Instant timestamp,
    String correlationId
) {
    /**
     * Factory method to create event metadata with generated timestamp and correlation ID.
     *
     * @param eventName the name of the event (e.g., "ProfileFetched")
     * @param topic the Kafka topic name
     * @param schemaVersion the event schema version
     * @return a new KafkaEventMetadata instance
     */
    public static KafkaEventMetadata of(String eventName, String topic, String schemaVersion) {
        return new KafkaEventMetadata(
            eventName,
            topic,
            schemaVersion,
            Instant.now(),
            UUID.randomUUID().toString()
        );
    }
}
