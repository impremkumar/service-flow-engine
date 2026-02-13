package com.ebtedge.service.flow.autoconfigure;

import lombok.Data;

@Data
public class ServiceFlowProperties {
    /** Enable or disable the Micrometer timers in the pipeline */
    private boolean metricsEnabled = true;

    /** Prefix for the exported metrics */
    private String metricName = "workflow.step.latency";
}
