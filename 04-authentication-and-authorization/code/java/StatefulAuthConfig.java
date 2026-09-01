// System Design - Backend
// Chapter 04, Authentication & Authorization -> 07 Stateful authentication
// Java 21 / Spring Boot 3.3 / Spring Security 6

@Configuration
class StatefulSecurityConfig {

    // Stateful: the id in the cookie is looked up in the session store
    // on every request. Spring Security's filter chain does that before
    // your controller runs, so this is configuration, not a filter.
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/health").permitAll()
                        .anyRequest().authenticated()) // 401 without one
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED));

        return http.build();
    }

    // The authenticated user then arrives as a parameter, rather than
    // being dug back out of a request attribute.
    @GetMapping("/me")
    Map<String, Object> me(@AuthenticationPrincipal UserDetails user) {
        return Map.of("id", user.getUsername(),
                "roles", user.getAuthorities());
    }
}
