// System Design - Backend
// Chapter 09, Caching -> 10.2 Rate limiting with Redis
// Java 21 / Spring Boot 3.3 / Spring Data Redis

package com.example.caching.ratelimit;

@Component
class RateLimitFilter extends OncePerRequestFilter {
    
    private static final int MAX_REQUESTS = 50;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    
    private final StringRedisTemplate redis;
    
    RateLimitFilter(StringRedisTemplate redis) {
        this.redis = redis;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
        throws ServletException, IOException {
        
        // The client IP as forwarded by the reverse proxy. Trust this
        // header only when the request came from a proxy you control,
        // or a caller simply forges it (ch 6).
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isBlank()) {
            clientIp = request.getRemoteAddr();
        }
        
        // one key per IP, per minute-long window
        long window = Instant.now().getEpochSecond() / 60;
        String key = "rate_limit:" + clientIp + ":" + window;
        
        // INCR is atomic, so concurrent requests cannot race here
        Long count = redis.opsForValue().increment(key);
        if (count == null) {
            chain.doFilter(request, response); // Redis down: fail open
            return;
        }
        
        if (count == 1L) {
            redis.expire(key, WINDOW); // TTL on the window's first hit
        }
        
        if (count > MAX_REQUESTS) {
            response.setHeader("Retry-After", "60");
            response.sendError(429, "Too Many Requests");
            return; // stop: the handler never runs
        }
        
        response.setHeader("X-RateLimit-Remaining",
            String.valueOf(MAX_REQUESTS - count));
        
        chain.doFilter(request, response); // and only now, continue
    }
}
// Two things worth knowing:
// * INCR then EXPIRE is two round trips. If the process dies between
// them the key never expires, so a Lua script or SET NX EX is the
// airtight version.
// * failing open (above) keeps the site up when Redis is down;
// failing closed protects the origin instead. Pick deliberately.
