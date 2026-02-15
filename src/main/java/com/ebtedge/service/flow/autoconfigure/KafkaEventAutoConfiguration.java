package com.ebtedge.service.flow.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Auto-configuration for Kafka event publishing.
 * Activated when service-flow.kafka.enabled=true.
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "service-flow.kafka", name = "enabled", havingValue = "true")
@EnableAsync
@EnableConfigurationProperties(ServiceFlowProperties.class)
public class KafkaEventAutoConfiguration {

    /**
     * Configures the Kafka producer factory with retry, timeout, and optimization settings.
     */
    @Bean
    public ProducerFactory<String, String> kafkaProducerFactory(ServiceFlowProperties properties) {
        Map<String, Object> config = new HashMap<>();

        ServiceFlowProperties.KafkaConfig kafka = properties.getKafka();

        // Basic configuration
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Retry & timeout configuration
        config.put(ProducerConfig.RETRIES_CONFIG, kafka.getRetryAttempts());
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafka.getTimeoutSeconds() * 1000);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, (kafka.getTimeoutSeconds() + 10) * 1000);

        // Producer optimizations
        config.put(ProducerConfig.ACKS_CONFIG, kafka.getProducer().getAcks());
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, kafka.getProducer().getBatchSize());
        config.put(ProducerConfig.LINGER_MS_CONFIG, kafka.getProducer().getLingerMs());
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, kafka.getProducer().getBufferMemory());
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, kafka.getProducer().getCompressionType());

        // Idempotence for exactly-once semantics
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        log.info("Kafka producer factory configured: servers={}, topic={}",
            kafka.getBootstrapServers(), kafka.getTopic());

        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * Creates a KafkaTemplate for sending messages to Kafka.
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> factory) {
        return new KafkaTemplate<>(factory);
    }

    /**
     * Configures the async thread pool executor for Kafka event publishing.
     */
    @Bean(name = "kafkaEventExecutor")
    public Executor kafkaEventExecutor(ServiceFlowProperties properties) {
        ServiceFlowProperties.KafkaConfig.AsyncConfig async = properties.getKafka().getAsync();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(async.getCorePoolSize());
        executor.setMaxPoolSize(async.getMaxPoolSize());
        executor.setQueueCapacity(async.getQueueCapacity());
        executor.setThreadNamePrefix(async.getThreadNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        log.info("Kafka event executor configured: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
            async.getCorePoolSize(), async.getMaxPoolSize(), async.getQueueCapacity());

        return executor;
    }

    /**
     * Configures ObjectMapper for JSON serialization of Kafka events.
     */
    @Bean(name = "kafkaObjectMapper")
    public ObjectMapper kafkaObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
