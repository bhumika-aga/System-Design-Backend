// System Design - Backend
// Chapter 04, Authentication & Authorization -> 17 Timing attacks
// Java 21 / Spring Boot 3.3 / Spring Security 6

@Service
class AuthenticationService {

    // A fixed, precomputed hash, used when no user was found so that
    // both paths pay the same cost.
    private static final String DUMMY_HASH = "$2a$12$abcdefghijklmnopqrstuv00000000000000000000000000000000";

    private static final Duration FLOOR = Duration.ofMillis(250);

    void authenticate(String email, String rawPassword) {
        long start = System.nanoTime();

        try {
            Optional<User> found = users.findByEmail(email);

            if (found.isEmpty()) {
                // hash anyway, so a missing user costs what a real one does
                encoder.matches(rawPassword, DUMMY_HASH);
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "authentication failed");
            }

            // matches() is constant-time internally
            if (!encoder.matches(rawPassword, found.get().passwordHash())) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "authentication failed");
            }

            // success: issue the session or the token here

        } finally {
            padToFloor(start); // every outcome takes about the same time
        }
    }

    private static void padToFloor(long startNanos) {
        long remaining = FLOOR.toNanos() - (System.nanoTime() - startNanos);
        if (remaining > 0) {
            try {
                Thread.sleep(Duration.ofNanos(remaining));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    // One message for every failure. Separate "no such user" and "wrong
    // password" errors hand an attacker a free account enumerator.
}
