// System Design - Backend
// Chapter 17, Backend Security -> 12 Security headers
// Java 21 / Spring Boot 3.3 / Spring Security 6

package com.example.security.headers;

@Configuration
class SecurityHeadersConfig {
    
    // Spring Security sets most of these already; this is the whole set
    // written out so nothing is left to chance.
    @Bean
    SecurityFilterChain securityHeaders(HttpSecurity http) throws Exception {
        
        http.headers(headers -> headers
                                    .frameOptions(frame -> frame.deny()) // X-Frame-Options
                                    .contentTypeOptions(Customizer.withDefaults()) // nosniff
                                    .referrerPolicy(referrer -> referrer.policy(
                                        ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                                    .httpStrictTransportSecurity(hsts -> hsts
                                                                             .maxAgeInSeconds(63_072_000)
                                                                             .includeSubDomains(true)
                                                                             .preload(true))
                                    .contentSecurityPolicy(csp -> csp.policyDirectives(
                                        "default-src 'self'; script-src 'self'; "
                                            + "object-src 'none'; base-uri 'self'")));
        
        return http.build();
    }
}

class SecurityHeadersConfigDemo {
    
    void demo() throws Exception {
        // Send HSTS only once you are certain every subdomain is on HTTPS.
        // With preload, browsers refuse plain HTTP for two years, and taking
        // it back is slow -- the header is a promise, not a setting.
    }
}
