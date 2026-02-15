package com.ebtedge.service.flow.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Map;

/**
 * Immutable record representing the complete Kafka event structure.
 * Contains both metadata (event context) and payload (event data).
 */
public record KafkaEventEnvelope(
    @JsonProperty("metadata") KafkaEventMetadata metadata,
    @JsonProperty("payload") Map<String, Object> payload
) {
    /**
     * Serializes this event envelope to JSON string.
     *
     * @return JSON string representation of the event
     * @throws JsonProcessingException if serialization fails
     */
    public String toJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper.writeValueAsString(this);
    }
}
