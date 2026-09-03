// System Design - Backend
// Chapter 19, Scaling & Performance II -> 01 Statelessness
// Java 21 / Spring Boot 3.3

package com.example.scaling2.sessions;

// Spring Session moves the HttpSession out of the process and into
// Redis, so any instance can serve any request. Add
// spring-session-data-redis and the configuration is the whole job:
//
//   spring.session.store-type: redis
//   spring.session.redis.namespace: app:session
//   server.servlet.session.timeout: 24h
//
// Controllers keep using HttpSession and never learn that it moved.
// When you want your own keys instead of the servlet session:
@Service
class Sessions {
    
    private final StringRedisTemplate redis;
    
    Sessions(StringRedisTemplate redis) {
        this.redis = redis;
    }
    
    void store(String sessionId, String userId) {
        redis.opsForValue().set("session:" + sessionId, userId,
            Duration.ofHours(24));
    }
    
    Optional<String> lookup(String sessionId) {
        // Every instance reads the same Redis, so it does not matter
        // which one the load balancer happened to pick.
        return Optional.ofNullable(
            redis.opsForValue().get("session:" + sessionId));
    }
}
