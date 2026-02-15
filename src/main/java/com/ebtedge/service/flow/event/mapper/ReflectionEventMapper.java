package com.ebtedge.service.flow.event.mapper;

import com.ebtedge.service.flow.event.EventMapper;
import com.ebtedge.service.flow.event.annotation.KafkaField;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic event mapper that uses reflection to extract fields annotated with @KafkaField.
 * This mapper eliminates the need to write manual mapper implementations for each domain class.
 *
 * <p>Features:
 * <ul>
 *   <li>Automatic field extraction based on @KafkaField annotations</li>
 *   <li>Support for both Java Records and regular classes</li>
 *   <li>Custom JSON field names via @KafkaField(name = "custom_name")</li>
 *   <li>Nested object support via @KafkaField(nested = true)</li>
 *   <li>Performance-optimized with reflection metadata caching</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * // Singleton instance for all types
 * .andPublishEvent("EventName", ReflectionEventMapper.INSTANCE)
 *
 * // Or type-specific instance (optional, for clarity)
 * .andPublishEvent("EventName", ReflectionEventMapper.forClass(ClientInfo.class))
 * </pre>
 *
 * @param <T> the type of the source object to map
 */
@Slf4j
public class ReflectionEventMapper<T> implements EventMapper<T> {

    /**
     * Singleton instance that works for all types.
     * Uses runtime type information to extract fields.
     * The wildcard type allows it to be used with any domain object.
     */
    @SuppressWarnings("rawtypes")
    public static final ReflectionEventMapper INSTANCE = new ReflectionEventMapper();

    /**
     * Cache of field metadata by class to avoid repeated reflection overhead.
     * Key: Class, Value: Array of FieldMetadata
     */
    private static final Map<Class<?>, FieldMetadata[]> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * Creates a type-specific instance (optional, for type safety and clarity).
     *
     * @param clazz the class to create a mapper for
     * @param <T> the type
     * @return a new ReflectionEventMapper instance
     */
    public static <T> ReflectionEventMapper<T> forClass(Class<T> clazz) {
        // Pre-warm the cache
        getFieldMetadata(clazz);
        return new ReflectionEventMapper<>();
    }

    @Override
    public Map<String, Object> map(T source) {
        if (source == null) {
            log.warn("Attempted to map null object, returning empty map");
            return new HashMap<>();
        }

        Class<?> sourceClass = source.getClass();
        FieldMetadata[] fields = getFieldMetadata(sourceClass);

        Map<String, Object> payload = new HashMap<>();

        for (FieldMetadata fieldMeta : fields) {
            try {
                Object value = fieldMeta.getValue(source);

                if (value != null) {
                    Object finalValue = value;

                    // Handle nested objects if configured
                    if (fieldMeta.nested && !isPrimitiveOrWrapper(value.getClass())) {
                        @SuppressWarnings("unchecked")
                        ReflectionEventMapper<Object> nestedMapper = (ReflectionEventMapper<Object>) this;
                        finalValue = nestedMapper.map(value);  // Recursive nested mapping
                    }

                    payload.put(fieldMeta.jsonName, finalValue);
                }
            } catch (Exception e) {
                log.error("Failed to extract field '{}' from {}: {}",
                    fieldMeta.fieldName, sourceClass.getSimpleName(), e.getMessage(), e);
            }
        }

        return payload;
    }

    /**
     * Gets field metadata for a class, using cache if available.
     */
    private static FieldMetadata[] getFieldMetadata(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, ReflectionEventMapper::extractFieldMetadata);
    }

    /**
     * Extracts field metadata from a class using reflection.
     * Supports both Java Records and regular classes.
     */
    private static FieldMetadata[] extractFieldMetadata(Class<?> clazz) {
        if (clazz.isRecord()) {
            return extractFromRecord(clazz);
        } else {
            return extractFromClass(clazz);
        }
    }

    /**
     * Extracts field metadata from a Java Record.
     */
    private static FieldMetadata[] extractFromRecord(Class<?> recordClass) {
        RecordComponent[] components = recordClass.getRecordComponents();

        return java.util.Arrays.stream(components)
            .filter(component -> component.isAnnotationPresent(KafkaField.class))
            .map(component -> {
                KafkaField annotation = component.getAnnotation(KafkaField.class);
                String jsonName = annotation.name().isEmpty() ? component.getName() : annotation.name();

                return new FieldMetadata(
                    component.getName(),
                    jsonName,
                    component.getAccessor(),
                    annotation.nested()
                );
            })
            .toArray(FieldMetadata[]::new);
    }

    /**
     * Extracts field metadata from a regular Java class.
     */
    private static FieldMetadata[] extractFromClass(Class<?> clazz) {
        java.util.List<FieldMetadata> metadataList = new java.util.ArrayList<>();

        // Walk up the class hierarchy to get all fields
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(KafkaField.class)) {
                    KafkaField annotation = field.getAnnotation(KafkaField.class);
                    String jsonName = annotation.name().isEmpty() ? field.getName() : annotation.name();

                    field.setAccessible(true);  // Allow access to private fields

                    metadataList.add(new FieldMetadata(
                        field.getName(),
                        jsonName,
                        field,
                        annotation.nested()
                    ));
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return metadataList.toArray(new FieldMetadata[0]);
    }

    /**
     * Checks if a class is a primitive type or wrapper.
     */
    private static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
               clazz == String.class ||
               clazz == Integer.class ||
               clazz == Long.class ||
               clazz == Double.class ||
               clazz == Float.class ||
               clazz == Boolean.class ||
               clazz == Character.class ||
               clazz == Byte.class ||
               clazz == Short.class ||
               Number.class.isAssignableFrom(clazz);
    }

    /**
     * Internal class to cache field metadata.
     */
    private static class FieldMetadata {
        final String fieldName;
        final String jsonName;
        final Object accessor;  // Either Method (for records) or Field (for classes)
        final boolean nested;

        FieldMetadata(String fieldName, String jsonName, Object accessor, boolean nested) {
            this.fieldName = fieldName;
            this.jsonName = jsonName;
            this.accessor = accessor;
            this.nested = nested;
        }

        Object getValue(Object source) throws Exception {
            if (accessor instanceof java.lang.reflect.Method method) {
                // Record accessor method
                return method.invoke(source);
            } else if (accessor instanceof Field field) {
                // Class field
                return field.get(source);
            }
            throw new IllegalStateException("Unknown accessor type: " + accessor.getClass());
        }
    }

    @Override
    public String schemaVersion() {
        return "1.0";
    }
}
