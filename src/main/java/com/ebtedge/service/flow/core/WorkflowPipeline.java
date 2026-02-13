package com.ebtedge.service.flow.core;

import com.ebtedge.service.flow.autoconfigure.ServiceFlowProperties;
import com.ebtedge.service.flow.exception.WorkflowException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkflowPipeline<T> {
    private final ResponseWrapper<T> currentResult;
    private final ServiceFlowProperties properties;
    private final MeterRegistry meterRegistry;

    private WorkflowPipeline(ResponseWrapper<T> result, ServiceFlowProperties properties, MeterRegistry meterRegistry) {
        this.currentResult = result;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Start a new workflow pipeline with configuration support.
     * Use WorkflowPipelineFactory in Spring-managed beans for automatic configuration.
     *
     * @param initialData The starting data
     * @param properties Configuration properties
     * @param meterRegistry Metrics registry
     * @param <T> The type of the initial data
     * @return A new WorkflowPipeline instance
     */
    public static <T> WorkflowPipeline<T> startWith(T initialData, ServiceFlowProperties properties, MeterRegistry meterRegistry) {
        log.debug("Starting workflow pipeline with initial data: {}", initialData);
        return new WorkflowPipeline<>(ResponseWrapper.success(initialData), properties, meterRegistry);
    }

    /**
     * Start a new workflow pipeline with default configuration (for backward compatibility).
     * Note: Metrics will always be enabled when using this method.
     *
     * @param initialData The starting data
     * @param <T> The type of the initial data
     * @return A new WorkflowPipeline instance
     * @deprecated Use WorkflowPipelineFactory.startWith() for configuration support
     */
    @Deprecated
    public static <T> WorkflowPipeline<T> startWith(T initialData) {
        log.debug("Starting workflow pipeline with initial data (deprecated method): {}", initialData);
        ServiceFlowProperties defaultProps = new ServiceFlowProperties();
        return new WorkflowPipeline<>(ResponseWrapper.success(initialData), defaultProps, Metrics.globalRegistry);
    }

    public <Next> WorkflowPipeline<Next> nextStep(String stepName, Function<T, ResponseWrapper<Next>> step) {
        if (!currentResult.isSuccess()) {
            log.debug("Skipping step '{}' due to previous failure: {}", stepName, currentResult.getError());
            return new WorkflowPipeline<>(ResponseWrapper.fail(currentResult.getError()), properties, meterRegistry);
        }

        log.info("Executing workflow step: {}", stepName);

        // Only record metrics if enabled
        Timer.Sample sample = properties.isMetricsEnabled()
            ? Timer.start(meterRegistry)
            : null;

        try {
            ResponseWrapper<Next> nextResult = step.apply(currentResult.getData());

            // Record metrics if enabled (deduplicated timer creation)
            long duration = 0;
            if (sample != null) {
                String status = nextResult.isSuccess() ? "SUCCESS" : "FAILURE";
                Timer timer = Timer.builder(properties.getMetricName())
                        .tag("step", stepName)
                        .tag("status", status)
                        .register(meterRegistry);
                duration = (long) sample.stop(timer);
            }

            if (nextResult.isSuccess()) {
                log.info("Step '{}' completed successfully{}ms", stepName,
                    sample != null ? " in " + duration : "");
            } else {
                log.warn("Step '{}' failed{} with error: {}", stepName,
                    sample != null ? " in " + duration + "ms" : "", nextResult.getError());
            }
            return new WorkflowPipeline<>(nextResult, properties, meterRegistry);
        } catch (Exception e) {
            if (sample != null) {
                Timer timer = Timer.builder(properties.getMetricName())
                        .tag("step", stepName)
                        .tag("status", "EXCEPTION")
                        .register(meterRegistry);
                sample.stop(timer);
            }
            log.error("Step '{}' threw exception: {}", stepName, e.getMessage(), e);
            throw e;
        }
    }

    public WorkflowPipeline<T> peek(Consumer<T> action) {
        if (currentResult.isSuccess()) {
            log.debug("Executing peek action with data: {}", currentResult.getData());
            action.accept(currentResult.getData());
        } else {
            log.debug("Skipping peek action due to failure state");
        }
        return this;
    }

    public <R> R mapToUI(Function<T, R> finalMapper) {
        if (!currentResult.isSuccess()) {
            log.error("Pipeline failed, throwing WorkflowException: {}", currentResult.getError());
            throw new WorkflowException(currentResult.getError());
        }
        log.info("Mapping workflow result to UI response");
        R result = finalMapper.apply(currentResult.getData());
        log.debug("Final UI response: {}", result);
        return result;
    }
}
