package com.ebtedge.service.flow.autoconfigure;


import com.ebtedge.service.flow.core.WorkflowPipelineFactory;
import com.ebtedge.service.flow.exception.GlobalHandler;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties(ServiceFlowProperties.class)
@Import(GlobalHandler.class) // Automatically enables the error handling framework
public class ServiceFlowAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServiceFlowProperties serviceFlowProperties() {
        return new ServiceFlowProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public WorkflowPipelineFactory workflowPipelineFactory(
            ServiceFlowProperties properties,
            MeterRegistry meterRegistry) {
        return new WorkflowPipelineFactory(properties, meterRegistry);
    }
}
