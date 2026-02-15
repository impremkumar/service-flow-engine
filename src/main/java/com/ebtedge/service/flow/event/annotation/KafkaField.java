package com.ebtedge.service.flow.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field or record component to be included in Kafka event payloads.
 * Fields without this annotation will be excluded from event serialization.
 *
 * <p>Example usage on a record:
 * <pre>
 * public record ClientInfo(
 *     &#64;KafkaField String clientId,
 *     &#64;KafkaField String name,
 *     String email,  // Not included in Kafka events
 *     &#64;KafkaField(name = "city_name") String city
 * ) {}
 * </pre>
 *
 * <p>Example usage on a class:
 * <pre>
 * public class UserData {
 *     &#64;KafkaField
 *     private String userId;
 *
 *     &#64;KafkaField(name = "user_email")
 *     private String email;
 *
 *     private String password;  // Not included
 * }
 * </pre>
 */
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface KafkaField {

    /**
     * Custom name for the field in the JSON payload.
     * If not specified, the actual field name is used.
     *
     * @return the custom JSON field name
     */
    String name() default "";

    /**
     * Whether to include this field in nested object serialization.
     * When true, if the field value is an object with @KafkaField annotations,
     * those nested fields will also be extracted.
     *
     * @return true to enable nested serialization
     */
    boolean nested() default false;
}
