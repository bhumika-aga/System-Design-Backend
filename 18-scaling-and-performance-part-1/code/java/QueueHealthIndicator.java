// System Design - Backend
// Chapter 18, Scaling & Performance -> 18 Load balancing
// Java 21 / Spring Boot 3.3

package com.example.scaling.health;

// Actuator already serves /actuator/health, and the database check
// comes with spring-boot-starter-data-jdbc. You add a check rather
// than write the endpoint:
@Component
class QueueHealthIndicator implements HealthIndicator {

    private final JdbcClient jdbc;

    QueueHealthIndicator(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Health health() {
        try {
            jdbc.sql("select 1").query(Integer.class).single();
            return Health.up().build();
        } catch (Exception e) {
            // DOWN makes the whole endpoint return 503, which is what
            // the load balancer is watching for.
            return Health.down()
                    .withDetail("reason", "database unreachable")
                    .build();
        }
    }
}
