package com.ebtedge.service.flow.autoconfigure;

import lombok.Data;

@Data
public class ServiceFlowProperties {
    /** Enable or disable the Micrometer timers in the pipeline */
    private boolean metricsEnabled = true;

    /** Prefix for the exported metrics */
    private String metricName = "workflow.step.latency";

    /** Kafka event publishing configuration */
    private KafkaConfig kafka = new KafkaConfig();

    @Data
    public static class KafkaConfig {
        /** Enable or disable Kafka event publishing */
        private boolean enabled = false;

        /** Kafka bootstrap servers */
        private String bootstrapServers = "localhost:9092";

        /** Kafka topic name for workflow events */
        private String topic = "workflow-events";

        /** Number of retry attempts for Kafka send failures */
        private int retryAttempts = 3;

        /** Timeout in seconds for Kafka send operation */
        private int timeoutSeconds = 100;

        /** Async thread pool configuration */
        private AsyncConfig async = new AsyncConfig();

        /** Kafka producer configuration */
        private ProducerConfig producer = new ProducerConfig();

        @Data
        public static class AsyncConfig {
            /** Core pool size for async executor */
            private int corePoolSize = 2;

            /** Maximum pool size for async executor */
            private int maxPoolSize = 10;

            /** Queue capacity for async executor */
            private int queueCapacity = 100;

            /** Thread name prefix for async executor */
            private String threadNamePrefix = "kafka-event-";
        }

        @Data
        public static class ProducerConfig {
            /** Number of acknowledgments required (0, 1, all) */
            private String acks = "1";

            /** Batch size in bytes */
            private int batchSize = 16384;

            /** Linger time in milliseconds */
            private int lingerMs = 10;

            /** Total memory buffer for producer */
            private int bufferMemory = 33554432;  // 32MB

            /** Compression type (none, gzip, snappy, lz4, zstd) */
            private String compressionType = "snappy";
        }
    }
}
