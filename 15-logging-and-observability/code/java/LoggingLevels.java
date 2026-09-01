// System Design - Backend
// Chapter 15, Logging & Observability -> 06 Environment-specific log levels
// Java 21 / Spring Boot 3.3 / Micrometer

package com.example.observability.logging;

// Level and format are CONFIGURATION in Spring Boot, not code.
// src/main/resources/logback-spring.xml:
//
//   <configuration>
//     <springProfile name="production">
//       <appender name="OUT" class="ch.qos.logback.core.ConsoleAppender">
//         <!-- JSON, parseable by Loki, ELK, New Relic -->
//         <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
//       </appender>
//       <root level="INFO"><appender-ref ref="OUT"/></root>
//     </springProfile>
//
//     <springProfile name="staging">
//       <root level="WARN"><appender-ref ref="OUT"/></root>
//     </springProfile>
//
//     <springProfile name="!production &amp; !staging">
//       <appender name="OUT" class="ch.qos.logback.core.ConsoleAppender">
//         <!-- local: human-readable and coloured -->
//         <encoder><pattern>
//           %d{HH:mm:ss} %highlight(%-5level) %logger{20} - %msg%n
//         </pattern></encoder>
//       </appender>
//       <root level="DEBUG"><appender-ref ref="OUT"/></root>
//     </springProfile>
//   </configuration>

// In the application there is just this, once per class. The logger
// is named after the class, so a level can be tuned per package:
//   logging.level.com.example.todo: DEBUG

@Service
class TodoService {

    private static final Logger log = LoggerFactory.getLogger(TodoService.class);
}

// No getLogLevel() switch anywhere, and no rebuild to change a level.
// The active profile decides, and a level can even be changed on a
// running service through the Actuator /loggers endpoint.
