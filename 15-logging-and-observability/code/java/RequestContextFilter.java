// System Design - Backend
// Chapter 15, Logging & Observability -> 11 Tracing middleware
// Java 21 / Spring Boot 3.3 / Micrometer

package com.example.observability.tracing;

// Spring Boot already opens a span for every incoming request. Add
// the starters and the tracing in this section exists:
//
//   spring-boot-starter-actuator
//   io.micrometer:micrometer-tracing-bridge-otel
//   io.opentelemetry:opentelemetry-exporter-otlp
//
// application.yml:
//   management:
//     tracing:
//       sampling:
//         probability: 1.0    # everything in dev; sample down in prod
//     otlp:
//       tracing:
//         endpoint: http://collector:4318/v1/traces
//
// Incoming W3C traceparent headers are continued automatically, so a
// trace that began at the load balancer keeps its id through this
// service and out into the next one.

// What you DO write is the extra business context on that span.
@Component
class RequestContextFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    RequestContextFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        Span span = tracer.currentSpan();
        if (span != null) {
            span.tag("http.user_agent",
                    String.valueOf(request.getHeader("User-Agent")));
            span.tag("user.id", currentUserId(request));
            // the trace id on every log line for this request
            MDC.put("traceId", span.context().traceId());
        }

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
