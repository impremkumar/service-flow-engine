package com.ebtedge.service.flow;

import com.ebtedge.service.flow.exception.GlobalHandler;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.util.function.Consumer;
import java.util.function.Function;

public class WorkflowPipeline<T> {
    private final ResponseWrapper<T> currentResult;

    private WorkflowPipeline(ResponseWrapper<T> result) {
        this.currentResult = result;
    }

    public static <T> WorkflowPipeline<T> startWith(T initialData) {
        return new WorkflowPipeline<>(ResponseWrapper.success(initialData));
    }

    public <Next> WorkflowPipeline<Next> nextStep(String stepName, Function<T, ResponseWrapper<Next>> step) {
        if (!currentResult.isSuccess()) {
            return new WorkflowPipeline<>(ResponseWrapper.fail(currentResult.getError()));
        }

        // Internal Framework Timer
        Timer.Sample sample = Timer.start(Metrics.globalRegistry);
        try {
            ResponseWrapper<Next> nextResult = step.apply(currentResult.getData());
            sample.stop(Timer.builder("workflow.step.latency")
                    .tag("step", stepName)
                    .tag("status", nextResult.isSuccess() ? "SUCCESS" : "FAILURE")
                    .register(Metrics.globalRegistry));
            return new WorkflowPipeline<>(nextResult);
        } catch (Exception e) {
            sample.stop(Timer.builder("workflow.step.latency").tag("step", stepName).tag("status", "EXCEPTION").register(Metrics.globalRegistry));
            throw e;
        }
    }

    public WorkflowPipeline<T> peek(Consumer<T> action) {
        if (currentResult.isSuccess()) action.accept(currentResult.getData());
        return this;
    }

    public <R> R mapToUI(Function<T, R> finalMapper) {
        if (!currentResult.isSuccess()) {
            throw new GlobalHandler(currentResult.getError());
        }
        return finalMapper.apply(currentResult.data());
    }
}
