// System Design - Backend
// Chapter 06, Controllers, Services & Repositories -> 15 Tracing & cancellation
// Java 21 / Spring Boot 3.3

package com.example.layers.filter;

@Component
class RequestIdFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(RequestIdFilter.class);
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
        throws ServletException, IOException {
        
        String id = UUID.randomUUID().toString();
        response.setHeader("X-Request-ID", id); // echo it back
        
        // MDC puts the id on EVERY log line produced during this
        // request, from any class, without threading an argument
        // through every method signature to get it there.
        MDC.put("requestId", id);
        try {
            log.info("{} {}", request.getMethod(),
                request.getRequestURI());
            chain.doFilter(request, response);
        } finally {
            MDC.clear(); // the thread goes back in the pool; left
            // set, it would tag the NEXT request too
        }
    }
}
