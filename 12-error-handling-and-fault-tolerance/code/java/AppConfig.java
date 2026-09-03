// System Design - Backend
// Chapter 12, Error Handling & Fault Tolerance -> 07 Configuration errors
// Java 21 / Spring Boot 3.3

package com.example.errors.config;

// Spring validates configuration while the context starts, so the
// app refuses to boot rather than failing on the first request that
// happens to need a missing value.
@ConfigurationProperties(prefix = "app")
@Validated
record AppConfig(
    @NotBlank String databaseUrl,
    @NotBlank String openaiApiKey,
    @NotBlank String jwtSecret,
    @NotBlank String resendApiKey) {
}

@SpringBootApplication
@EnableConfigurationProperties(AppConfig.class)
class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
// A missing value fails the context and Spring says exactly what is
// wrong before exiting:
//
// ***************************
// APPLICATION FAILED TO START
// ***************************
// Binding to target AppConfig failed:
// Property: app.jwtSecret
// Reason: must not be blank
//
// application.yml binds the values, the environment supplies them,
// and nothing secret is ever committed:
// app:
// database-url: ${DATABASE_URL}
// jwt-secret: ${JWT_SECRET}
