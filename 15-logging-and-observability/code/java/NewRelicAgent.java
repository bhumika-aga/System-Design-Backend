// System Design - Backend
// Chapter 15, Logging & Observability -> 14 New Relic agent setup
// Java 21 / Spring Boot 3.3 / Micrometer

package com.example.observability.vendor;

// On the JVM the agent attaches at startup, so there is no code at
// all: no import, no line in main(), no framework middleware.
//
// java -javaagent:/opt/newrelic/newrelic.jar -jar app.jar
//
// newrelic.yml, or the equivalent environment variables:
// common:
// app_name: todo-api
// license_key: ${NEW_RELIC_LICENSE_KEY}
// application_logging:
// forwarding:
// enabled: true # ship logs with their trace ids attached
// distributed_tracing:
// enabled: true
//
// The agent instruments Spring MVC, JDBC, the HTTP clients and the
// thread pools by rewriting bytecode as classes load, which is why
// an unmodified jar comes out fully traced.
//
// Prefer the vendor-neutral path where you can. Exporting OTLP from
// Micrometer, as in the metrics and tracing sections above, keeps the
// same instrumentation working if you ever change vendor -- swapping
// an agent means re-learning its configuration from scratch.
