package com.ebtedge.service.flow.autoconfigure;


import com.ebtedge.service.flow.core.WorkflowPipelineFactory;
import com.ebtedge.service.flow.exception.GlobalHandler;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties
@Import(GlobalHandler.class) // Automatically enables the error handling framework
public class ServiceFlowAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "service-flow")
    public ServiceFlowProperties serviceFlowProperties() {
        return new ServiceFlowProperties();
    }
}
