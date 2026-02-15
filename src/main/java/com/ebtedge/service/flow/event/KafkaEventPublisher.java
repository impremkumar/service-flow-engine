package com.ebtedge.service.flow.event;

import com.ebtedge.service.flow.autoconfigure.ServiceFlowProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for asynchronously publishing events to Kafka with metrics and retry logic.
 * All publishing operations are fire-and-forget and do not block the calling thread.
 * This bean is only created when Kafka is enabled.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "service-flow.kafka", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ServiceFlowProperties properties;
    private final MeterRegistry meterRegistry;
    @Qualifier("kafkaObjectMapper")
    private final ObjectMapper objectMapper;

    /**
     * Asynchronously publishes an event to Kafka with metrics.
     * This method runs on a dedicated thread pool and does not block the caller.
     * Retry logic is handled by Kafka producer configuration (retries=3).
     *
     * @param eventName the name of the event (e.g., "ProfileFetched")
     * @param payload the event payload as a map
     * @param schemaVersion the event schema version
     * @return a CompletableFuture that completes when the event is sent (or fails)
     */
    @Async("kafkaEventExecutor")
    public CompletableFuture<SendResult<String, String>> publishAsync(
            String eventName,
            Map<String, Object> payload,
            String schemaVersion) {

        Timer.Sample sample = Timer.start(meterRegistry);
        String topic = properties.getKafka().getTopic();

        try {
            // Create event envelope with metadata
            KafkaEventMetadata metadata = KafkaEventMetadata.of(eventName, topic, schemaVersion);
            KafkaEventEnvelope envelope = new KafkaEventEnvelope(metadata, payload);

            // Serialize to JSON
            String json = objectMapper.writeValueAsString(envelope);

            log.debug("Publishing Kafka event: eventName={}, topic={}, correlationId={}",
                eventName, topic, metadata.correlationId());

            // Send to Kafka (returns CompletableFuture)
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, metadata.correlationId(), json)
                .toCompletableFuture();

            // Add callbacks for metrics and logging
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    recordMetric(sample, eventName, topic, "FAILURE");
                    recordCounter(eventName, topic, "FAILURE");
                    log.error("Failed to publish Kafka event: eventName={}, topic={}, error={}",
                        eventName, topic, ex.getMessage(), ex);
                } else {
                    recordMetric(sample, eventName, topic, "SUCCESS");
                    recordCounter(eventName, topic, "SUCCESS");
                    log.info("Kafka event published successfully: eventName={}, topic={}, partition={}, offset={}",
                        eventName, topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            });

            return future;

        } catch (JsonProcessingException e) {
            recordMetric(sample, eventName, topic, "SERIALIZATION_ERROR");
            recordCounter(eventName, topic, "SERIALIZATION_ERROR");
            log.error("Failed to serialize event payload: eventName={}, topic={}", eventName, topic, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Records a timer metric for event publishing latency.
     */
    private void recordMetric(Timer.Sample sample, String eventName, String topic, String status) {
        if (properties.isMetricsEnabled()) {
            Timer timer = Timer.builder("workflow.kafka.event.latency")
                .tag("event_name", eventName)
                .tag("topic", topic)
                .tag("status", status)
                .register(meterRegistry);
            sample.stop(timer);
        }
    }

    /**
     * Records a counter metric for event publishing count.
     */
    private void recordCounter(String eventName, String topic, String status) {
        if (properties.isMetricsEnabled()) {
            Counter.builder("workflow.kafka.event.count")
                .tag("event_name", eventName)
                .tag("topic", topic)
                .tag("status", status)
                .register(meterRegistry)
                .increment();
        }
    }
}
