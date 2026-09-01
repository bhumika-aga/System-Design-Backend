// System Design - Backend
// Chapter 06, Controllers, Services & Repositories -> 10 The next() function
// Java 21 / Spring Boot 3.3

package com.example.layers.filter;

// Spring's equivalent of middleware is a filter, and calling
// chain.doFilter(...) is next(): pass execution along the chain.
@Component
class LoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        log.info("{} {}", request.getMethod(), request.getRequestURI());

        // EARLY EXIT: short-circuit without ever calling the chain.
        if ("yes".equals(request.getHeader("X-Blocked"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return; // the request stops here
        }

        chain.doFilter(request, response); // === next() ===
    }
}
// Extend OncePerRequestFilter rather than implementing Filter: a
// plain filter runs again on a forward or an async dispatch, and you
// would log the same request twice.
