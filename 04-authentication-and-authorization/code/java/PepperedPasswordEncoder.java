// System Design - Backend
// Chapter 04, Authentication & Authorization -> 04 Sessions, Argon2 + pepper
// Java 21 / Spring Boot 3.3 / Spring Security 6

@Configuration
class PasswordEncoderConfig {
    
    // Argon2 is the modern default: memory-hard, so an attacker's GPUs
    // buy far less than they do against bcrypt.
    @Bean
    PasswordEncoder passwordEncoder() {
        // saltLength, hashLength, parallelism, memoryKb, iterations
        // tune the last three until one hash costs about 250ms
        return new Argon2PasswordEncoder(16, 32, 4, 64 * 1024, 3);
    }
    
    // A pepper is a secret the database never holds, so a stolen dump on
    // its own cannot be cracked. It belongs in the environment.
}

@Component
class PepperedPasswordEncoder implements PasswordEncoder {
    
    private final PasswordEncoder delegate = new Argon2PasswordEncoder(
        16,
        32,
        4,
        64 * 1024,
        3);
    private final SecretKeySpec pepper;
    
    PepperedPasswordEncoder(@Value("${PASSWORD_PEPPER}") String secret) {
        this.pepper = new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
    
    // HMAC the password with the pepper before it ever reaches Argon2
    private String peppered(CharSequence raw) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(pepper);
            return Base64.getEncoder().encodeToString(
                mac.doFinal(raw.toString()
                                .getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
    
    @Override
    public String encode(CharSequence raw) {
        return delegate.encode(peppered(raw));
    }
    
    @Override
    public boolean matches(CharSequence raw, String encoded) {
        return delegate.matches(peppered(raw), encoded);
    }
}
