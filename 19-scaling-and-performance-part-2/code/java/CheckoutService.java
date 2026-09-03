// System Design - Backend
// Chapter 19, Scaling & Performance II -> 12 Scaling mental models
// Java 21 / Spring Boot 3.3

package com.example.scaling2.metrics;

// Micrometer is the metrics façade; Actuator exposes it. Add
// micrometer-registry-prometheus and /actuator/prometheus appears
// for Prometheus to scrape and Grafana to draw.
//
//   management.endpoints.web.exposure.include: health,prometheus
//   management.metrics.distribution:
//     percentiles-histogram.http.server.requests: true
//
// Every HTTP request is already timed under http_server_requests.
// Add your own timer where a business step matters more than a route:
@Service
class CheckoutService {
    
    private final MeterRegistry meters;
    
    CheckoutService(MeterRegistry meters) {
        this.meters = meters;
    }
    
    Receipt checkout(Cart cart) {
        return Timer.builder("checkout.duration")
                   .publishPercentiles(0.5, 0.9, 0.99)
                   .register(meters)
                   .record(() -> settle(cart));
    }
}
