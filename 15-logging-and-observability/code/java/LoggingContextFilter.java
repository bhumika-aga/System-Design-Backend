// System Design - Backend
// Chapter 15, Logging & Observability -> 11 Logger setup + shared fields
// Java 21 / Spring Boot 3.3 / Micrometer

package com.example.observability.logging;

// Fields that belong on EVERY entry are configuration, not something
// each call site remembers to repeat.
//
// logback-spring.xml:
//   <encoder class="net.logstash.logback.encoder.LogstashEncoder">
//     <customFields>
//       {"service":"todo-api","env":"${APP_ENV}"}
//     </customFields>
//   </encoder>

// Per-REQUEST values go in the MDC. Set them once in a filter and
// every log line produced anywhere in that request carries them,
// without being passed down as an argument.
@Component
class LoggingContextFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
        throws ServletException, IOException {
        
        MDC.put("requestId", UUID.randomUUID().toString());
        MDC.put("userId", currentUserId(request));
        
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear(); // the thread is pooled; leaving values set
            // would tag the NEXT request with them
        }
    }
}
