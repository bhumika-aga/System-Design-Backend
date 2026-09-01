// System Design - Backend
// Chapter 04, Authentication & Authorization -> 05 JWT, RS256 + refresh rotation
// Java 21 / Spring Boot 3.3 / Spring Security 6

@Configuration
class Rs256DecoderConfig {

    // RS256: verify with the PUBLIC key, and pin the algorithm so a
    // token claiming a different one is refused outright.
    @Bean
    JwtDecoder rs256Decoder(RSAPublicKey publicKey) {
        return NimbusJwtDecoder.withPublicKey(publicKey)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
    }
    // In production you point at the issuer's JWKS instead and Spring
    // fetches, caches and rotates the keys for you:
    // spring.security.oauth2.resourceserver.jwt.jwk-set-uri: ...

}

@Service
class RefreshService {

    private final StringRedisTemplate redis;

    RefreshService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    // Rotate a refresh token, and catch reuse of a spent one.
    String rotate(String presented) {
        String family = redis.opsForValue().get("rt:" + presented);

        if (family == null) {
            // not live -- but was it already spent? then it was stolen
            String spent = redis.opsForValue().get("spent:" + presented);
            if (spent != null) {
                redis.delete("family:" + spent); // revoke every token
                throw new IllegalStateException("refresh reuse detected");
            }
            throw new IllegalStateException("invalid refresh token");
        }

        redis.delete("rt:" + presented); // consume this one
        redis.opsForValue().set("spent:" + presented, family,
                Duration.ofDays(14)); // and remember it

        String rotated = newOpaqueToken();
        redis.opsForValue().set("rt:" + rotated, family,
                Duration.ofDays(14));
        return rotated;
    }
}
