# Kafka Event Field Selection Guide

This document explains how to control which fields are included in Kafka events using two approaches.

## Approach 1: Annotation-Based (Recommended) ⭐

Use `@KafkaField` annotations on your domain classes to declaratively mark which fields should be included in Kafka events.

### Benefits
- ✅ **No manual mappers needed** - just annotate fields
- ✅ **Self-documenting** - clear which fields go to Kafka
- ✅ **Less code** - centralized reflection-based mapping
- ✅ **Privacy by default** - only annotated fields are included

### Example: Simple Record

```java
import com.ebtedge.service.flow.event.annotation.KafkaField;

public record ClientInfo(
    @KafkaField String clientId,
    @KafkaField String name,
    String email,  // NOT annotated = excluded from Kafka events
    @KafkaField String city
) {}
```

**Usage:**
```java
.andPublishEvent("ClientInfoFetched", ReflectionEventMapper.INSTANCE)
```

**Resulting JSON:**
```json
{
  "metadata": {
    "eventName": "ClientInfoFetched",
    "topic": "workflow-events",
    "schemaVersion": "1.0",
    "timestamp": "2026-02-14T21:45:00Z",
    "correlationId": "abc-123-def"
  },
  "payload": {
    "clientId": "CUST-7788",
    "name": "John Doe",
    "city": "New York"
  }
}
```

Notice: `email` is **excluded** (privacy/security).

---

### Example: Custom JSON Field Names

```java
public record UserProfile(
    @KafkaField(name = "user_id") String userId,
    @KafkaField(name = "full_name") String fullName,
    @KafkaField(name = "account_status") String status
) {}
```

**Resulting JSON:**
```json
{
  "payload": {
    "user_id": "USER-123",
    "full_name": "Jane Smith",
    "account_status": "ACTIVE"
  }
}
```

---

### Example: Nested Objects

```java
public record Address(
    @KafkaField String street,
    @KafkaField String city,
    String internalCode  // excluded
) {}

public record Customer(
    @KafkaField String customerId,
    @KafkaField(nested = true) Address address  // nested=true extracts @KafkaField from Address
) {}
```

**Usage:**
```java
.andPublishEvent("CustomerCreated", ReflectionEventMapper.INSTANCE)
```

**Resulting JSON:**
```json
{
  "payload": {
    "customerId": "CUST-456",
    "address": {
      "street": "123 Main St",
      "city": "Boston"
    }
  }
}
```

Notice: `address.internalCode` is **excluded** because it's not annotated.

---

### Example: Real-World UIResponse (Already Implemented)

```java
// Balance.java
public record Balance(
    @KafkaField String clientId,
    @KafkaField double amount
) {}

// Demographics.java
public record Demographics(
    @KafkaField String name,
    String email,  // EXCLUDED for privacy
    @KafkaField String city
) {}

// UIResponse.java
public record UIResponse(
    @KafkaField(nested = true) Balance balance,
    @KafkaField(nested = true) Demographics demographics
) {}
```

**Usage in Controller:**
```java
UIResponse response = pipelineFactory.startWith(accountId)
    .nextStep("FetchBalance", id -> serviceA.getBalance(id))
    .nextStep("FetchDemographics", bal -> ...)
    .mapToUI(profileData -> new UIResponse(...))
    .andPublishEvent("ProfileFetched", ReflectionEventMapper.INSTANCE);
```

**Resulting JSON:**
```json
{
  "payload": {
    "balance": {
      "clientId": "CUST-7788",
      "amount": 1250.50
    },
    "demographics": {
      "name": "Jane Doe",
      "city": "New York"
    }
  }
}
```

---

## Approach 2: Manual Mapper (For Complex Cases)

When you need custom transformation logic, write a manual `EventMapper` implementation.

### When to Use Manual Mappers
- ❌ **NOT for simple field selection** (use annotations instead)
- ✅ **Complex transformations** (concatenate fields, format values, conditional logic)
- ✅ **Computed fields** (add timestamps, derive values, aggregations)
- ✅ **Legacy compatibility** (specific JSON structure required)

### Example: Manual Mapper with Custom Logic

```java
public enum OrderEventMapper implements EventMapper<Order> {
    INSTANCE;

    @Override
    public Map<String, Object> map(Order order) {
        Map<String, Object> payload = new HashMap<>();

        // Custom transformation logic
        payload.put("orderId", order.id().toUpperCase());
        payload.put("totalAmount", order.calculateTotal());  // computed field
        payload.put("status", order.status().name().toLowerCase());

        // Conditional logic
        if (order.isPremium()) {
            payload.put("priorityLevel", "HIGH");
        }

        // Date formatting
        payload.put("orderDate", order.createdAt().toString());

        return payload;
    }
}
```

**Usage:**
```java
.andPublishEvent("OrderPlaced", OrderEventMapper.INSTANCE)
```

---

## Comparison: Which Approach to Use?

| Scenario | Use Annotation-Based | Use Manual Mapper |
|----------|---------------------|-------------------|
| Simple field selection | ✅ Yes | ❌ No (overkill) |
| Privacy/exclude sensitive fields | ✅ Yes | ✅ Yes |
| Nested objects | ✅ Yes (with `nested=true`) | ✅ Yes |
| Custom JSON field names | ✅ Yes (with `name` attribute) | ✅ Yes |
| Field transformations (uppercase, format) | ❌ No | ✅ Yes |
| Computed fields (calculations, aggregations) | ❌ No | ✅ Yes |
| Conditional logic | ❌ No | ✅ Yes |
| Concatenate multiple fields | ❌ No | ✅ Yes |

---

## Performance Considerations

### Annotation-Based Mapper
- **First call**: Uses reflection to discover fields (~few milliseconds)
- **Subsequent calls**: Uses cached metadata (~microseconds)
- **Verdict**: Excellent performance for production use

### Manual Mapper
- **Always**: Direct field access (fastest possible)
- **Verdict**: Marginally faster, but annotation-based is fast enough

**Recommendation**: Use annotation-based unless you have extreme performance requirements (millions of events/second).

---

## Best Practices

### ✅ DO
- Use `@KafkaField` for simple field selection (90% of cases)
- Exclude sensitive data (passwords, SSNs, API keys) by NOT annotating them
- Use `nested=true` for nested objects
- Use custom `name` for JSON field naming conventions
- Write manual mappers for complex transformations

### ❌ DON'T
- Don't annotate sensitive fields (email, password, etc.)
- Don't use manual mappers for simple field selection
- Don't forget to add `nested=true` when you want nested field extraction
- Don't mix both approaches for the same event (pick one per event type)

---

## Migration Guide: From Manual to Annotation-Based

**Before (Manual Mapper):**
```java
public enum UserEventMapper implements EventMapper<User> {
    INSTANCE;

    @Override
    public Map<String, Object> map(User user) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", user.userId());
        payload.put("name", user.name());
        payload.put("city", user.city());
        // Don't include email
        return payload;
    }
}
```

**After (Annotation-Based):**
```java
public record User(
    @KafkaField String userId,
    @KafkaField String name,
    String email,  // excluded
    @KafkaField String city
) {}

// Delete UserEventMapper.java - no longer needed!

// Usage:
.andPublishEvent("UserCreated", ReflectionEventMapper.INSTANCE)
```

**Result**:
- ✅ Deleted 15 lines of boilerplate code
- ✅ Self-documenting domain model
- ✅ Easier to maintain

---

## Summary

**90% of use cases**: Use `@KafkaField` with `ReflectionEventMapper.INSTANCE`

**10% of use cases**: Write custom `EventMapper` for complex transformations

**Current implementation**: Both `UIResponse`, `Balance`, and `Demographics` are already annotated and ready to use with `ReflectionEventMapper.INSTANCE`!
