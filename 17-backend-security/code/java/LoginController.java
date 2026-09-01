// System Design - Backend
// Chapter 17, Backend Security -> 14 Complete secure login
// Java 21 / Spring Boot 3.3 / Spring Security 6

package com.example.security.login;

@RestController
class LoginController {

        private final UserRepository users;
        private final PasswordEncoder encoder;
        private final StringRedisTemplate redis;

        LoginController(UserRepository users, PasswordEncoder encoder,
                        StringRedisTemplate redis) {
                this.users = users;
                this.encoder = encoder;
                this.redis = redis;
        }

        @PostMapping("/login")
        ResponseEntity<Void> login(@Valid @RequestBody LoginRequest req) {

                // 1. Format validated by @Valid before the method runs
                // (record below): the first line of defence.

                // 2. A bound parameter, so no SQL injection is possible
                Optional<User> found = users.findByEmail(req.email());

                // 3. One generic failure. Never reveal whether the email
                // exists, and hash even when it does not so the two paths
                // take the same time (ch 12).
                if (found.isEmpty()
                                || !encoder.matches(req.password(),
                                                found.get().passwordHash())) {
                        encoder.matches(req.password(), DUMMY_HASH);
                        throw new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED, "invalid email or password");
                }

                // 4. A session id from a cryptographically secure source
                byte[] bytes = new byte[32];
                SecureRandom.getInstanceStrong().nextBytes(bytes); // not Random
                String sessionId = Base64.getUrlEncoder()
                                .withoutPadding().encodeToString(bytes);

                // 5. Server-side session, with an expiry
                redis.opsForValue().set("session:" + sessionId,
                                found.get().id(), Duration.ofDays(7));

                // 6. The cookie, with all three flags
                ResponseCookie cookie = ResponseCookie.from("SESSION", sessionId)
                                .httpOnly(true).secure(true).sameSite("Strict")
                                .maxAge(Duration.ofDays(7)).path("/")
                                .build();

                return ResponseEntity.noContent()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .build();
        }
}

record LoginRequest(
                @Email @NotBlank String email,
                @NotBlank @Size(min = 8) String password) {
}
