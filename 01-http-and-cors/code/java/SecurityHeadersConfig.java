// System Design - Backend
// Chapter 01, HTTP & CORS -> Security headers: the attack each one stops
// Java 21 / Spring Boot 3.3

package com.example.http;

@Configuration
class SecurityHeadersConfig {
    
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {
        
        http.headers(headers -> headers
                                    .httpStrictTransportSecurity(hsts -> hsts
                                                                             .maxAgeInSeconds(31_536_000)
                                                                             .includeSubDomains(true))
                                    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                                    .frameOptions(frame -> frame.deny())
                                    // X-Content-Type-Options: nosniff
                                    .contentTypeOptions(Customizer.withDefaults()));
        
        // The filter chain runs before your controller, so these land while
        // the response is still uncommitted (see sec 3).
        return http.build();
    }
}
