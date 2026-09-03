// System Design - Backend
// Chapter 09, Caching -> 10.1 Lazy (cache-aside) caching
// Java 21 / Spring Boot 3.3 / Spring Data Redis

package com.example.caching.cache;

// Spring names these strategies directly, so the annotation you
// reach for IS the strategy from earlier in this chapter:
//   @Cacheable -> cache-aside (lazy)    @CachePut -> write-through
@Service
class ProductService {
    
    private final ProductRepository products;
    
    ProductService(ProductRepository products) {
        this.products = products;
    }
    
    // CACHE-ASIDE. Spring checks the cache first; on a HIT the method
    // body never runs at all. On a MISS the body runs and whatever it
    // returns is stored under "product::<id>".
    @Cacheable(cacheNames = "product", key = "#productId")
    public Product get(String productId) {
        // reached only on a miss -- this is the expensive path
        return products.findById(productId).orElseThrow();
    }
    
    // WRITE-THROUGH. The body ALWAYS runs, and its result replaces the
    // cached value, so the database and the cache cannot drift apart.
    @CachePut(cacheNames = "product", key = "#product.id()")
    public Product update(Product product) {
        return products.save(product);
    }
    
    // The third of the family: drop the entry and let the next read
    // repopulate it.
    @CacheEvict(cacheNames = "product", key = "#productId")
    public void delete(String productId) {
        products.deleteById(productId);
    }
    
    // The TTL is configuration, not code. application.yml:
    // spring:
    // cache:
    // type: redis
    // redis:
    // time-to-live: 1h
    // and @EnableCaching on a @Configuration class switches it all on.
    
    // The same steps written out, when you want to see the mechanism
    // rather than let the proxy do it:
    Product getExplicitly(String id) throws JsonProcessingException {
        String key = "product:" + id;
        
        String cached = redis.opsForValue().get(key); // 1. try the cache
        if (cached != null) {
            return mapper.readValue(cached, Product.class);
        }
        
        Product product = products.findById(id).orElseThrow(); // 2. miss
        
        redis.opsForValue().set(key, // 3. store with TTL
            mapper.writeValueAsString(product), Duration.ofHours(1));
        
        return product;
    }
    // Careful: calling get() from another method of THIS class bypasses
    // the proxy, so nothing is cached. Same trap as @Transactional.
}
