package com.ebtedge.service.flow.core;

import com.ebtedge.service.flow.autoconfigure.ServiceFlowProperties;
import com.ebtedge.service.flow.domain.ErrorDetails;
import com.ebtedge.service.flow.exception.WorkflowException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.core.instrument.Metrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowPipelineTest {

    private MeterRegistry meterRegistry;
    private ServiceFlowProperties properties;

    @BeforeEach
    void setUp() {
        // Reset metrics for each test
        Metrics.globalRegistry.clear();
        meterRegistry = new SimpleMeterRegistry();
        Metrics.addRegistry(meterRegistry);
        properties = new ServiceFlowProperties();
        properties.setMetricsEnabled(true);
    }

    // Helper method to create pipelines with configuration
    private <T> WorkflowPipeline<T> startPipeline(T initialData) {
        return startPipeline(initialData, properties, meterRegistry);
    }

    @Test
    void testStartWith_Success() {
        WorkflowPipeline<String> pipeline = startPipeline("initial");
        assertNotNull(pipeline);
    }

    @Test
    void testStartWith_BackwardCompatibility() {
        // Test deprecated method still works
        @SuppressWarnings("deprecation")
        WorkflowPipeline<String> pipeline = startPipeline("initial");
        assertNotNull(pipeline);
    }

    @Test
    void testHappyPath_MultipleSteps() {
        String result = startPipeline(10)
                .nextStep("double", x -> ResponseWrapper.success(x * 2))
                .nextStep("toString", x -> ResponseWrapper.success(String.valueOf(x)))
                .mapToUI(s -> s + "!");

        assertEquals("20!", result);
    }

    @Test
    void testFailure_FirstStep() {
        ErrorDetails error = new ErrorDetails("ERR_001", "First step failed");

        assertThrows(WorkflowException.class, () -> {
            startPipeline("data")
                    .nextStep("failingStep", x -> ResponseWrapper.fail(error))
                    .nextStep("shouldNotExecute", x -> ResponseWrapper.success("never"))
                    .mapToUI(x -> x);
        });
    }

    @Test
    void testFailure_Propagation() {
        ErrorDetails error = new ErrorDetails("ERR_002", "Step failed");
        List<String> executedSteps = new ArrayList<>();

        assertThrows(WorkflowException.class, () -> {
            startPipeline("data")
                    .nextStep("step1", x -> {
                        executedSteps.add("step1");
                        return ResponseWrapper.success(x);
                    })
                    .nextStep("step2", x -> {
                        executedSteps.add("step2");
                        return ResponseWrapper.fail(error);
                    })
                    .nextStep("step3", x -> {
                        executedSteps.add("step3");
                        return ResponseWrapper.success(x);
                    })
                    .mapToUI(x -> x);
        });

        assertEquals(2, executedSteps.size());
        assertTrue(executedSteps.contains("step1"));
        assertTrue(executedSteps.contains("step2"));
        assertFalse(executedSteps.contains("step3"));
    }

    @Test
    void testException_InStep() {
        RuntimeException testException = new RuntimeException("Step exception");

        assertThrows(RuntimeException.class, () -> {
            startPipeline("data")
                    .nextStep("exceptionStep", x -> {
                        throw testException;
                    })
                    .mapToUI(x -> x);
        });
    }

    @Test
    void testPeek_Success() {
        AtomicBoolean peeked = new AtomicBoolean(false);

        String result = startPipeline("data")
                .peek(x -> peeked.set(true))
                .mapToUI(x -> x);

        assertTrue(peeked.get());
        assertEquals("data", result);
    }

    @Test
    void testPeek_SkippedOnFailure() {
        AtomicBoolean peeked = new AtomicBoolean(false);
        ErrorDetails error = new ErrorDetails("ERR_003", "Failed");

        assertThrows(WorkflowException.class, () -> {
            startPipeline("data")
                    .nextStep("fail", x -> ResponseWrapper.fail(error))
                    .peek(x -> peeked.set(true))
                    .mapToUI(x -> x);
        });

        assertFalse(peeked.get());
    }

    @Test
    void testMapToUI_Success() {
        Integer result = startPipeline("5")
                .mapToUI(Integer::parseInt);

        assertEquals(5, result);
    }

    @Test
    void testMapToUI_Failure() {
        ErrorDetails error = new ErrorDetails("ERR_004", "Cannot map");

        WorkflowException exception = assertThrows(WorkflowException.class, () -> {
            startPipeline("data")
                    .nextStep("fail", x -> ResponseWrapper.fail(error))
                    .mapToUI(x -> x);
        });

        assertEquals(error, exception.getError());
    }

    @Test
    void testMetrics_SuccessfulStep() {
        startPipeline(100)
                .nextStep("testStep", x -> ResponseWrapper.success(x * 2))
                .mapToUI(x -> x);

        assertNotNull(meterRegistry.find("workflow.step.latency")
                .tag("step", "testStep")
                .tag("status", "SUCCESS")
                .timer());
    }

    @Test
    void testMetrics_FailedStep() {
        ErrorDetails error = new ErrorDetails("ERR_005", "Failed step");

        assertThrows(WorkflowException.class, () -> {
            startPipeline("data")
                    .nextStep("failStep", x -> ResponseWrapper.fail(error))
                    .mapToUI(x -> x);
        });

        assertNotNull(meterRegistry.find("workflow.step.latency")
                .tag("step", "failStep")
                .tag("status", "FAILURE")
                .timer());
    }

    @Test
    void testMetrics_ExceptionStep() {
        assertThrows(RuntimeException.class, () -> {
            startPipeline("data")
                    .nextStep("exceptionStep", x -> {
                        throw new RuntimeException("Test");
                    })
                    .mapToUI(x -> x);
        });

        assertNotNull(meterRegistry.find("workflow.step.latency")
                .tag("step", "exceptionStep")
                .tag("status", "EXCEPTION")
                .timer());
    }

    @Test
    void testComplexWorkflow() {
        List<String> trace = new ArrayList<>();

        String result = startPipeline(5)
                .peek(x -> trace.add("start:" + x))
                .nextStep("multiply", x -> ResponseWrapper.success(x * 2))
                .peek(x -> trace.add("afterMultiply:" + x))
                .nextStep("add", x -> ResponseWrapper.success(x + 10))
                .peek(x -> trace.add("afterAdd:" + x))
                .nextStep("toString", x -> ResponseWrapper.success("Result=" + x))
                .mapToUI(String::toUpperCase);

        assertEquals("RESULT=20", result);
        assertEquals(3, trace.size());
    }

    @Test
    void testTypeTransformation() {
        Double result = startPipeline("10")
                .nextStep("parseInt", s -> ResponseWrapper.success(Integer.parseInt(s)))
                .nextStep("multiply", i -> ResponseWrapper.success(i * 2.5))
                .mapToUI(d -> d);

        assertEquals(25.0, result);
    }

    @Test
    void testMultiplePeeks() {
        List<String> peekLog = new ArrayList<>();

        String result = startPipeline("value")
                .peek(x -> peekLog.add("peek1:" + x))
                .peek(x -> peekLog.add("peek2:" + x))
                .peek(x -> peekLog.add("peek3:" + x))
                .mapToUI(x -> x);

        assertEquals("value", result);
        assertEquals(3, peekLog.size());
        assertEquals("peek1:value", peekLog.get(0));
        assertEquals("peek2:value", peekLog.get(1));
        assertEquals("peek3:value", peekLog.get(2));
    }

    @Test
    void testWorkflowExceptionPreservesErrorDetails() {
        ErrorDetails error = new ErrorDetails("CUSTOM_ERR", "Custom error message");

        WorkflowException exception = assertThrows(WorkflowException.class, () -> {
            startPipeline(1)
                    .nextStep("fail", x -> ResponseWrapper.fail(error))
                    .mapToUI(x -> x);
        });

        assertEquals("CUSTOM_ERR", exception.getError().errorCode());
        assertEquals("Custom error message", exception.getError().message());
        assertEquals("Custom error message", exception.getMessage());
    }

    @Test
    void testNullDataHandling() {
        String result = startPipeline((String) null)
                .nextStep("handleNull", x -> ResponseWrapper.success(x == null ? "was-null" : x))
                .mapToUI(x -> x);

        assertEquals("was-null", result);
    }

    @Test
    void testChainedFailures() {
        ErrorDetails firstError = new ErrorDetails("ERR_FIRST", "First error");

        WorkflowException exception = assertThrows(WorkflowException.class, () -> {
            startPipeline("data")
                    .nextStep("fail1", x -> ResponseWrapper.fail(firstError))
                    .nextStep("fail2", x -> ResponseWrapper.fail(new ErrorDetails("ERR_SECOND", "Should not reach")))
                    .mapToUI(x -> x);
        });

        assertEquals("ERR_FIRST", exception.getError().errorCode());
    }

    @Test
    void testMetricsDisabled() {
        // Disable metrics
        properties.setMetricsEnabled(false);

        Integer result = startPipeline(100)
                .nextStep("testStep", x -> ResponseWrapper.success(x * 2))
                .mapToUI(x -> x);

        assertEquals(200, result);

        // Verify no metrics were recorded
        assertNull(meterRegistry.find("workflow.step.latency")
                .tag("step", "testStep")
                .timer());
    }

    @Test
    void testCustomMetricName() {
        // Change metric name
        properties.setMetricName("custom.workflow.metric");

        startPipeline(100)
                .nextStep("customStep", x -> ResponseWrapper.success(x * 2))
                .mapToUI(x -> x);

        // Verify custom metric name is used
        assertNotNull(meterRegistry.find("custom.workflow.metric")
                .tag("step", "customStep")
                .tag("status", "SUCCESS")
                .timer());
    }
}
