# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

This is a Maven project (wrapper included). Java 17 required.

```bash
./mvnw clean install        # Build and run tests
./mvnw test                 # Run all tests
./mvnw spring-boot:run      # Run the application
./mvnw test -Dtest=ServiceFlowEngineApplicationTests  # Run a single test class
```

## Architecture

**Service Flow Engine** is a Spring Boot 4.0.2 application implementing a functional pipeline pattern for resilient service orchestration with built-in observability.

### Core Pattern: WorkflowPipeline + ResponseWrapper

The central abstraction is `WorkflowPipeline<T>`, a generic functional pipeline that chains service calls using railway-oriented programming. Each step produces a `ResponseWrapper<T>` which encodes success/failure as a monadic result type, short-circuiting on errors.

Pipeline usage pattern:
```
WorkflowPipeline.startWith(input)
    .nextStep("StepName", transformFunction)   // each step is timed via Micrometer
    .peek(sideEffect)
    .mapToUI(mappingFunction)
```

Every `nextStep` call automatically records a `workflow.step.latency` Micrometer timer with `step` and `status` (SUCCESS/FAILURE/EXCEPTION) tags.

### Key Packages

- **`com.ebtedge.service.flow`** — Pipeline core (`WorkflowPipeline`, `ResponseWrapper`), REST controller (`FinancialController`)
- **`com.ebtedge.service.flow.domain`** — Java records: `Balance`, `Demographics`, `UIResponse`, `ErrorDetails`
- **`com.ebtedge.service.flow.service`** — Service layer (`MockServiceA`, `MockServiceB`) providing mock implementations
- **`com.ebtedge.service.flow.exception`** — `GlobalHandler` (`@RestControllerAdvice`) catches `WorkflowException` and returns HTTP 400
- **`com.ebtedge.service.flow.autoconfigure`** — Spring Boot auto-configuration; registers `GlobalHandler` bean and binds `service-flow.*` properties

### API Endpoint

`GET /api/profile/{accountId}` — Fetches balance (MockServiceA), extracts clientId, fetches demographics (MockServiceB), combines into `UIResponse`.

### Auto-Configuration

The project is structured as a reusable Spring Boot auto-configuration module. `ServiceFlowAutoConfiguration` is registered via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. Configuration properties use prefix `service-flow` (e.g., `service-flow.metrics-enabled`).
