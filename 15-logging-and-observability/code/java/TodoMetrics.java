// System Design - Backend
// Chapter 15, Logging & Observability -> 11 Metrics instrumentation
// Java 21 / Spring Boot 3.3 / Micrometer

package com.example.observability.metrics;

// Spring Boot already exports http.server.requests with method, uri
// and status tags, and latency percentiles with it. Adding the
// registry is the entire job:
//
//   io.micrometer:micrometer-registry-prometheus
//
// application.yml:
//   management:
//     endpoints.web.exposure.include: health,prometheus
//     metrics.tags.application: todo-api
//     metrics.distribution.percentiles-histogram.http.server.requests: true
//
// GET /actuator/prometheus then serves the scrape endpoint.
//
// Note the tag is `uri`, not `path`: Micrometer records the ROUTE
// TEMPLATE (/todos/{id}), never the concrete URL. Recording the raw
// path would give every single id its own time series and melt
// Prometheus -- the cardinality trap this chapter warns about.

// A BUSINESS metric is the part you actually have to write.
@Service
class TodoMetrics {

    private final Counter created;

    TodoMetrics(MeterRegistry registry) {
        this.created = Counter.builder("todos_created_total")
                .description("Todos created")
                .tag("service", "todo-api")
                .register(registry);
    }

    void recordCreated() {
        created.increment();
    }
}
