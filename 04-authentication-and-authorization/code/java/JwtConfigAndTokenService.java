// System Design - Backend
// Chapter 04, Authentication & Authorization -> 05 JSON Web Tokens
// Java 21 / Spring Boot 3.3 / Spring Security 6

@Configuration
class JwtConfig {

    private final SecretKey key = new SecretKeySpec(
            System.getenv("JWT_SECRET").getBytes(StandardCharsets.UTF_8),
            "HmacSHA256");

    @Bean
    JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256) // pin the algorithm
                .build(); // stops "alg: none"
    }
}

@Service
class TokenService {

    private final JwtEncoder encoder;

    TokenService(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    // mint a self-contained token carrying the claims
    String sign(String userId, String role) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userId) // sub
                .claim("role", role) // for authorization
                .issuedAt(now) // iat
                .expiresAt(now.plus(1, ChronoUnit.HOURS)) // exp
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }
}
// Verification is not code you write: the resource-server filter runs
// jwtDecoder() on every request and rejects bad tokens before your
// controller is ever called.
