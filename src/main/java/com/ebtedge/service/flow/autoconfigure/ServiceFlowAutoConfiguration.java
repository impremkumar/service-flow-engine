package com.ebtedge.service.flow.autoconfigure;


import com.ebtedge.service.flow.exception.GlobalHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(GlobalHandler.class) // Automatically enables the error handling framework
public class ServiceFlowAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServiceFlowProperties serviceFlowProperties() {
        return new ServiceFlowProperties();
    }
}
