// System Design - Backend
// Chapter 04, Authentication & Authorization -> 06 Cookies
// Java 21 / Spring Boot 3.3 / Spring Security 6

@Configuration
class SecurityConfig {
    
    // Both defences below are Spring Security defaults. This is what
    // those defaults amount to, written out.
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Double-submit CSRF: the token goes out in a cookie and must
            // come back in a header. Safe methods are exempt; anything
            // that writes is not.
            .csrf(csrf -> csrf.csrfTokenRepository(
                CookieCsrfTokenRepository.withHttpOnlyFalse()))
            
            // Session fixation: on a successful login Spring issues a
            // brand-new session id and copies the attributes across, so a
            // pre-login id planted by an attacker is worthless.
            .sessionManagement(session -> session
                                              .sessionFixation(fixation -> fixation.newSession()));
        
        return http.build();
    }
}
