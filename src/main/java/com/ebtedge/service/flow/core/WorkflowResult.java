package com.ebtedge.service.flow.core;

import com.ebtedge.service.flow.autoconfigure.ServiceFlowProperties;
import com.ebtedge.service.flow.event.EventMapper;
import com.ebtedge.service.flow.event.KafkaEventPublishingService;
import lombok.RequiredArgsConstructor;

/**
 * Wrapper for WorkflowPipeline.mapToUI() result that enables optional event publishing.
 * Supports fluent API chaining: .mapToUI(...).andPublishEvent(...) or .mapToUI(...).result()
 *
 * @param <R> the type of the wrapped result
 */
@RequiredArgsConstructor
public class WorkflowResult<R> {
    private final R result;
    private final ServiceFlowProperties properties;
    private final KafkaEventPublishingService kafkaPublishingService;

    /**
     * Chains event publishing after mapToUI and returns the unwrapped result.
     * This is the primary method for integrating Kafka event publishing into the workflow.
     *
     * @param eventName the name of the event (e.g., "ProfileFetched")
     * @param mapper the event mapper for field selection
     * @param <T> the type of the result (extends R for type safety)
     * @return the unwrapped result for controller use
     */
    @SuppressWarnings("unchecked")
    public <T extends R> T andPublishEvent(String eventName, EventMapper<R> mapper) {
        if (kafkaPublishingService != null) {
            kafkaPublishingService.publishIfEnabled(eventName, result, mapper);
        }
        return (T) result;
    }

    /**
     * Returns the unwrapped result without publishing an event.
     * Use this method when event publishing is not needed for a particular workflow.
     *
     * @return the unwrapped result
     */
    public R result() {
        return result;
    }
}
