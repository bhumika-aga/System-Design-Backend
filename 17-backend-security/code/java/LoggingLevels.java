// System Design - Backend
// Chapter 17, Backend Security -> 12 Misconfigurations
// Java 21 / Spring Boot 3.3 / Spring Security 6

package com.example.security.logging;

class LoggingNotesDemo {
    
    void demo() throws Exception {
        // Level per environment, in logback-spring.xml, so DEBUG cannot
        // reach production by accident:
        //
        // <springProfile name="production">
        // <root level="INFO"><appender-ref ref="JSON"/></root>
        // </springProfile>
        // <springProfile name="!production">
        // <root level="DEBUG"><appender-ref ref="CONSOLE"/></root>
        // </springProfile>
        
        // The security point is not the level, it is what you put in the
        // message. DEBUG lines survive into logs, log shipping, and search
        // indexes, and they routinely carry exactly what an attacker wants.
        log.debug("login attempt for {} with {}", email, password); // NEVER
        log.debug("login attempt userId={}", user.id()); // fine
        
        // Spring Boot's own DEBUG output includes bound request parameters,
        // so turning it on globally in production leaks payloads even when
        // your code is careful:
        // logging.level.org.springframework.web: DEBUG <- not in prod
    }
}
