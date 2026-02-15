package com.ebtedge.service.flow.event;

import com.ebtedge.service.flow.autoconfigure.ServiceFlowProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Facade service for publishing Kafka events with conditional enabling.
 * Provides fire-and-forget publishing that does not impact the calling workflow.
 * This bean is always created, but delegates to KafkaEventPublisher only when available.
 */
@Slf4j
@Service
public class KafkaEventPublishingService {

    @Autowired(required = false)
    private KafkaEventPublisher publisher;

    private final ServiceFlowProperties properties;

    public KafkaEventPublishingService(ServiceFlowProperties properties) {
        this.properties = properties;
    }

    /**
     * Publishes an event to Kafka only if Kafka is enabled in configuration.
     * This is a fire-and-forget operation: failures are logged but not propagated.
     *
     * @param eventName the name of the event (e.g., "ProfileFetched")
     * @param data the source data object to map
     * @param mapper the event mapper for field selection
     * @param <T> the type of the source data object
     */
    public <T> void publishIfEnabled(String eventName, T data, EventMapper<T> mapper) {
        if (!properties.getKafka().isEnabled() || publisher == null) {
            log.trace("Kafka publishing disabled or publisher not available, skipping event: {}", eventName);
            return;
        }

        try {
            // Map the data to event payload
            Map<String, Object> payload = mapper.map(data);
            String schemaVersion = mapper.schemaVersion();

            // Fire-and-forget: we don't wait for the CompletableFuture
            publisher.publishAsync(eventName, payload, schemaVersion);

            log.debug("Initiated Kafka event publishing: eventName={}", eventName);

        } catch (Exception e) {
            // Swallow exceptions to prevent impact on HTTP response
            log.error("Failed to initiate Kafka event publishing for event={}: {}",
                eventName, e.getMessage(), e);
        }
    }
}
