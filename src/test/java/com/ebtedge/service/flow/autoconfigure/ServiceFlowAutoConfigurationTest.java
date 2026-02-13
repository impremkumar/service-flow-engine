package com.ebtedge.service.flow.autoconfigure;

import com.ebtedge.service.flow.exception.GlobalHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceFlowAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ServiceFlowAutoConfiguration.class));

    @Test
    void testAutoConfigurationLoads() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ServiceFlowAutoConfiguration.class);
        });
    }

    @Test
    void testGlobalHandlerBeanCreated() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(GlobalHandler.class);
        });
    }

    @Test
    void testServiceFlowPropertiesBeanCreated() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ServiceFlowProperties.class);
        });
    }

    @Test
    void testServiceFlowPropertiesDefaultValues() {
        contextRunner.run(context -> {
            ServiceFlowProperties properties = context.getBean(ServiceFlowProperties.class);
            assertThat(properties).isNotNull();
            assertThat(properties.isMetricsEnabled()).isTrue();
            assertThat(properties.getMetricName()).isEqualTo("workflow.step.latency");
        });
    }

    @Test
    void testServiceFlowPropertiesCanBeOverridden() {
        contextRunner
                .withPropertyValues(
                        "service-flow.metrics-enabled=false",
                        "service-flow.metric-name=custom.metric"
                )
                .run(context -> {
                    ServiceFlowProperties properties = context.getBean(ServiceFlowProperties.class);
                    assertThat(properties.isMetricsEnabled()).isFalse();
                    assertThat(properties.getMetricName()).isEqualTo("custom.metric");
                });
    }

    @Test
    void testConditionalBeanCreation() {
        contextRunner
                .withBean(ServiceFlowProperties.class, () -> {
                    ServiceFlowProperties custom = new ServiceFlowProperties();
                    custom.setMetricsEnabled(false);
                    return custom;
                })
                .run(context -> {
                    assertThat(context).hasSingleBean(ServiceFlowProperties.class);
                    ServiceFlowProperties properties = context.getBean(ServiceFlowProperties.class);
                    assertThat(properties.isMetricsEnabled()).isFalse();
                });
    }
}
