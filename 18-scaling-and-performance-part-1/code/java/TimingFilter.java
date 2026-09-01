// System Design - Backend
// Chapter 18, Scaling & Performance -> 06 Finding bottlenecks
// Java 21 / Spring Boot 3.3

package com.example.scaling.bottlenecks;

// Logs how long every request took. A filter sees the request on the
// way in and the response on the way out, which is exactly the pair
// of moments you need to time it.
@Component
class TimingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TimingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        long start = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            // The status is already on the response by the time the
            // chain returns, so there is nothing to wrap or capture.
            long ms = (System.nanoTime() - start) / 1_000_000;
            log.info("method={} path={} status={} duration={}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    ms);
        }
    }
}
