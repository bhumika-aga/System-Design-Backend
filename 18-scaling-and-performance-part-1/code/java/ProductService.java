// System Design - Backend
// Chapter 18, Scaling & Performance -> 12 Caching fundamentals
// Java 21 / Spring Boot 3.3

package com.example.scaling.caching;

@Service
class ProductService {

    private final ProductRepository products;
    private final StringRedisTemplate redis;
    private final ObjectMapper json;

    // L1: in this process. Microseconds, but every instance has its
    // own copy, so keep the TTL short enough to tolerate the drift.
    private final Cache<String, Product> local = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofSeconds(30))
            .build();

    ProductService(ProductRepository products, StringRedisTemplate redis,
            ObjectMapper json) {
        this.products = products;
        this.redis = redis;
        this.json = json;
    }

    Product find(String id) throws Exception {
        Product hit = local.getIfPresent(id);
        if (hit != null) {
            return hit; // L1 hit, ~0.001ms
        }

        // L2: shared by every instance. Survives a redeploy.
        String key = "product:" + id;
        String cached = redis.opsForValue().get(key);
        if (cached != null) {
            Product p = json.readValue(cached, Product.class);
            local.put(id, p);
            return p; // L2 hit, ~2ms
        }

        // Miss on both. Pay for the database once, then fill both.
        Product p = products.findById(id).orElseThrow();
        redis.opsForValue().set(key, json.writeValueAsString(p),
                Duration.ofMinutes(10));
        local.put(id, p);
        return p;
    }
}
