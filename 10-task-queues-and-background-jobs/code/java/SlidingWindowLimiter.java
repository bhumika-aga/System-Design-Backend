// System Design - Backend
// Chapter 10, Task Queues & Background Jobs -> 18 Sliding-window rate limiting
// Java 21 / Spring Boot 3.3

package com.example.tasks.ratelimit;

@Component
class SlidingWindowLimiter {

    // One Lua script, executed atomically by Redis: nothing can
    // interleave between the trim, the count and the insert.
    // A pipeline does NOT give you that; it only saves round trips.
    private static final String LUA = """
            redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[1])
            local count = redis.call('ZCARD', KEYS[1])
            if count < tonumber(ARGV[3]) then
              redis.call('ZADD', KEYS[1], ARGV[2], ARGV[2])
              redis.call('PEXPIRE', KEYS[1], ARGV[4])
              return 1
            end
            return 0
            """;

    private final StringRedisTemplate redis;
    private final RedisScript<Long> script = new DefaultRedisScript<>(LUA, Long.class);

    SlidingWindowLimiter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    boolean allow(String key, int limit, Duration window) {
        long now = Instant.now().toEpochMilli();
        long windowStart = now - window.toMillis();

        Long allowed = redis.execute(script, List.of(key),
                String.valueOf(windowStart),
                String.valueOf(now),
                String.valueOf(limit),
                String.valueOf(window.toMillis() * 2));

        return Long.valueOf(1L).equals(allowed);
    }
}

@Component
class EmailRateLimitedConsumer {

    // In the consumer: refuse the work and let the retry policy space it
    // out. A plain exception is right here -- the message is not bad, it
    // is merely early.
    @RabbitListener(queues = "email.send")
    void handle(EmailPayload payload) {
        if (!limiter.allow("rate:email", 100, Duration.ofMinutes(1))) {
            throw new IllegalStateException("rate limit exceeded");
        }
        emails.send(payload);
    }
}
