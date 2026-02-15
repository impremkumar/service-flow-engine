package com.ebtedge.service.flow.event;

import java.util.Map;

/**
 * Functional interface for mapping domain objects to Kafka event payloads.
 * Implementations control which fields from the source object are included in the event.
 *
 * @param <T> the type of the source object to map
 */
@FunctionalInterface
public interface EventMapper<T> {

    /**
     * Maps a source object to a Kafka event payload.
     * The returned map should contain only the fields that should be published to Kafka,
     * enabling flexible field selection and excluding sensitive data.
     *
     * @param source the source object to map
     * @return a map containing the selected fields for the Kafka event payload
     */
    Map<String, Object> map(T source);

    /**
     * Returns the schema version for this event mapper.
     * Used for event schema evolution and versioning.
     *
     * @return the schema version (default: "1.0")
     */
    default String schemaVersion() {
        return "1.0";
    }
}
