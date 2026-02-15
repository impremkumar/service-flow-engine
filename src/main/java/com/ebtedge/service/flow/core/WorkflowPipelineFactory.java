package com.ebtedge.service.flow.core;

import com.ebtedge.service.flow.autoconfigure.ServiceFlowProperties;
import com.ebtedge.service.flow.event.KafkaEventPublishingService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory for creating WorkflowPipeline instances with proper configuration.
 * This allows the pipeline to respect ServiceFlowProperties settings and inject Kafka event publishing.
 */
@Component
@RequiredArgsConstructor
public class WorkflowPipelineFactory {

    private final ServiceFlowProperties properties;
    private final MeterRegistry meterRegistry;

    @Autowired(required = false)
    private KafkaEventPublishingService kafkaPublishingService;

    /**
     * Create a new workflow pipeline with initial data.
     *
     * @param initialData The starting data for the pipeline
     * @param <T> The type of the initial data
     * @return A configured WorkflowPipeline instance
     */
    public <T> WorkflowPipeline<T> startWith(T initialData) {
        return WorkflowPipeline.startWith(initialData, properties, meterRegistry, kafkaPublishingService);
    }
}
