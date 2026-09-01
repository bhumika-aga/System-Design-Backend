// System Design - Backend
// Chapter 01, HTTP & CORS -> Flow 2: the preflighted request
// Java 21 / Spring Boot 3.3

package com.example.http;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // exact origin, never *, once credentials are allowed
                .allowedOrigins("https://example.com")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                .allowedHeaders("Content-Type", "Authorization")
                .allowCredentials(true)
                .maxAge(86_400); // cache the approval for 24h
    }
}
// Spring answers the OPTIONS preflight itself, before routing, so your
// handler never runs for one.
