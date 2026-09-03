// System Design - Backend
// Chapter 04, Authentication & Authorization -> 04 Sessions
// Java 21 / Spring Boot 3.3 / Spring Security 6

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 900) // 15 min
class SessionConfig {
    
    @Bean
    PasswordEncoder passwordEncoder() {
        // BCrypt salts every password itself, and is slow on purpose
        return new BCryptPasswordEncoder();
    }
}

@RestController
class LoginController {
    
    private final PasswordEncoder encoder;
    private final UserRepository users;
    
    LoginController(PasswordEncoder encoder, UserRepository users) {
        this.encoder = encoder;
        this.users = users;
    }
    
    @PostMapping("/login")
    ResponseEntity<Void> login(@RequestBody Credentials creds,
                               HttpServletRequest request) {
        
        User user = users.findByEmail(creds.email())
                        .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED, "authentication failed"));
        
        // matches() is constant-time internally
        if (!encoder.matches(creds.password(), user.passwordHash())) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "authentication failed");
        }
        
        HttpSession session = request.getSession(true); // new session
        session.setAttribute("userId", user.id());
        session.setAttribute("role", user.role());
        return ResponseEntity.noContent().build();
    }
}
// Spring Session writes the session to Redis; the cookie is
// configuration, not code. application.yml:
// server.servlet.session.cookie:
// http-only: true # JS cannot read it
// secure: true # HTTPS only
// same-site: lax
