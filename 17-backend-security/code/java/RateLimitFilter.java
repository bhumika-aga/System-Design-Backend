// System Design - Backend
// Chapter 17, Backend Security -> 08 Rate limiting
// Java 21 / Spring Boot 3.3 / Spring Security 6

package com.example.security.ratelimit;

class RateLimitFilterDemo {

    void demo() throws Exception {
        // A ConcurrentHashMap keyed by IP grows without bound and is per
        // instance. In production back it with Redis (bucket4j-redis), which
        // also makes the limit apply across the whole fleet rather than
        // letting an attacker get N times the quota from N replicas.
    }
}

// Bucket4j: a token bucket per client, held in a cache.
@Component
class RateLimitFilter extends OncePerRequestFilter {

    // 5 requests per second, burst of 10
    private static final Supplier<Bucket> NEW_BUCKET = () -> Bucket.builder()
            .addLimit(limit -> limit.capacity(10)
                    .refillGreedy(5, Duration.ofSeconds(1)))
            .build();

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        Bucket bucket = buckets.computeIfAbsent(
                clientIp(request), key -> NEW_BUCKET.get());

        if (!bucket.tryConsume(1)) {
            response.setHeader("Retry-After", "1");
            response.sendError(429, "too many requests");
            return;
        }
        chain.doFilter(request, response);
    }
}
