// System Design - Backend
// Chapter 19, Scaling & Performance II -> 03 Health checks
// Java 21 / Spring Boot 3.3

package com.example.scaling2.health;

// A real health check tests what you depend on, not just "I am alive".
// Actuator aggregates every HealthIndicator on the classpath, so the
// database and Redis checks arrive with their starters, and the
// endpoint returns 503 the moment any one of them reports DOWN:
//
//   management.endpoint.health.show-details: always
//   management.endpoint.health.group.readiness.include: db,redis
//
// Write an indicator only for things Actuator cannot know about:
@Component
class BrokerHealthIndicator implements HealthIndicator {

    private final RabbitTemplate rabbit;

    BrokerHealthIndicator(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    @Override
    public Health health() {
        try {
            rabbit.execute(channel -> channel.queueDeclarePassive("emails"));
            return Health.up().build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("broker", "unreachable")
                    .build();
        }
    }
}
