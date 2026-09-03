// System Design - Backend
// Chapter 14, Configuration Management -> 9.1 Config loading & validation
// Java 21 / Spring Boot 3.3

package com.example.config;

// All runtime settings, as one immutable record. @Validated makes
// Spring check every constraint while the context starts, so a bad
// value stops the boot instead of surfacing on some later request.
@ConfigurationProperties(prefix = "app")
@Validated
record AppConfig(
    
    // Application settings, with defaults in application.yml
    @Min(1) @Max(65535) int port,
    
    @Pattern(regexp = "debug|info|warn|error") String logLevel,
    
    @NotNull Environment env, // an enum: invalid values die here
    
    @Valid @NotNull Database database,
    @Valid @NotNull External external,
    @Valid @NotNull Features features) {
    
    enum Environment {
        DEVELOPMENT, STAGING, PRODUCTION
    }
    
    // Nesting mirrors the YAML instead of flattening it into
    // DB_HOST, DB_PORT, DB_USER... one prefix per concern.
    record Database(
        @NotBlank String host,
        @Min(1) @Max(65535) int port,
        @NotBlank String user,
        @NotBlank String password,
        @NotBlank String name,
        @Min(1) int poolSize) {
        
        // Derived, not stored: one place builds the URL.
        String url() {
            return "postgres://%s:%s@%s:%d/%s"
                       .formatted(user, password, host, port, name);
        }
    }
    
    record External(@NotBlank String stripeApiKey) {
    }
    
    record Features(boolean newCheckoutEnabled) {
    }
}

@SpringBootApplication
@EnableConfigurationProperties(AppConfig.class)
class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// application.yml holds the shape and the non-secret defaults;
// the environment supplies the secrets, so nothing sensitive is
// ever committed:
//
// app:
// port: 8080
// log-level: info
// env: development
// database:
// host: localhost
// port: 5432
// user: ${DB_USER}
// password: ${DB_PASSWORD} # from the environment only
// name: ecommerce
// pool-size: 10
// external:
// stripe-api-key: ${STRIPE_API_KEY}
// features:
// new-checkout-enabled: false
//
// Per-environment overrides live in application-<profile>.yml and
// need no code at all:
//
// # application-production.yml
// app:
// log-level: info
// database:
// pool-size: 50
//
// Started with --spring.profiles.active=production, Spring layers
// that file over the base one. There is no if (env == "production")
// anywhere in the application.
