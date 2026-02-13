package com.ebtedge.service.flow.core;

import com.ebtedge.service.flow.autoconfigure.ServiceFlowProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowPipelineFactoryTest {

    private WorkflowPipelineFactory factory;
    private ServiceFlowProperties properties;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        properties = new ServiceFlowProperties();
        properties.setMetricsEnabled(true);
        meterRegistry = new SimpleMeterRegistry();
        factory = new WorkflowPipelineFactory(properties, meterRegistry);
    }

    @Test
    void testStartWithCreatesWorkflowPipeline() {
        WorkflowPipeline<String> pipeline = factory.startWith("test-data");
        assertNotNull(pipeline);
    }

    @Test
    void testFactoryUsesConfiguration() {
        properties.setMetricsEnabled(true);
        properties.setMetricName("custom.metric");

        Integer result = factory.startWith(10)
                .nextStep("doubleIt", x -> ResponseWrapper.success(x * 2))
                .mapToUI(x -> x);

        assertEquals(20, result);

        // Verify custom metric name is used
        assertNotNull(meterRegistry.find("custom.metric")
                .tag("step", "doubleIt")
                .timer());
    }

    @Test
    void testFactoryRespectsMetricsDisabled() {
        properties.setMetricsEnabled(false);

        Integer result = factory.startWith(5)
                .nextStep("multiplyBy3", x -> ResponseWrapper.success(x * 3))
                .mapToUI(x -> x);

        assertEquals(15, result);

        // Verify no metrics were recorded
        assertNull(meterRegistry.find("workflow.step.latency")
                .tag("step", "multiplyBy3")
                .timer());
    }

    @Test
    void testFactoryWithDifferentDataTypes() {
        WorkflowPipeline<Integer> intPipeline = factory.startWith(100);
        WorkflowPipeline<String> stringPipeline = factory.startWith("hello");
        WorkflowPipeline<Boolean> boolPipeline = factory.startWith(true);

        assertNotNull(intPipeline);
        assertNotNull(stringPipeline);
        assertNotNull(boolPipeline);
    }
}
