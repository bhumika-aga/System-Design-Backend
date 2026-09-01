// System Design - Backend
// Chapter 12, Error Handling & Fault Tolerance -> 08 Deep health checks
// Java 21 / Spring Boot 3.3

package com.example.errors.health;

// Actuator already health-checks the DataSource and Redis. Write an
// indicator only for what it cannot know about.
@Component
class PaymentGatewayHealthIndicator implements HealthIndicator {

    private final RestClient payments;

    PaymentGatewayHealthIndicator(RestClient payments) {
        this.payments = payments;
    }

    @Override
    public Health health() {
        try {
            payments.get().uri("/ping").retrieve().toBodilessEntity();
            return Health.up().build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
// application.yml:
// management:
// endpoint:
// health:
// show-details: when-authorized # never "always" in public
// probes:
// enabled: true # adds /health/readiness and /liveness
//
// GET /actuator/health answers 200 when everything is UP and 503
// when anything is DOWN: the same contract as a hand-written
// handler, with the database and cache checks already written.
//
// Readiness and liveness are not the same question. Liveness asks
// "should this process be restarted?"; readiness asks "should it
// receive traffic right now?". A database blip is a readiness
// failure -- restarting the pod would not have helped.
