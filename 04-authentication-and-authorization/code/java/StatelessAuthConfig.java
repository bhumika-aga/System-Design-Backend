// System Design - Backend
// Chapter 04, Authentication & Authorization -> 08 Stateless authentication
// Java 21 / Spring Boot 3.3 / Spring Security 6

@Configuration
class StatelessSecurityConfig {
    
    // Stateless: identity comes from the verified token and there is no
    // store lookup at all. Declaring the resource server is the whole job.
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session -> session
                                              .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                                               .anyRequest().authenticated())
            .oauth2ResourceServer(oauth -> oauth
                                               .jwt(Customizer.withDefaults())); // uses jwtDecoder()
        
        return http.build();
    }
    
    // The claims of the verified token, with nothing fetched from a store.
    @GetMapping("/me")
    Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("id", jwt.getSubject(), // the "sub" claim
            "role", jwt.getClaimAsString("role"));
    }
}
