// System Design - Backend
// Chapter 12, Error Handling & Fault Tolerance -> 14 Safe logging practices
// Java 21 / Spring Boot 3.3

package com.example.errors.logging;

@Service
class LoginService {

        private static final Logger log = LoggerFactory.getLogger(LoginService.class);

        void onFailedLogin(User user, LoginRequest request, AppConfig config) {

                // UNSAFE, never do this
                log.error("login_failed email={} password={} apiKey={}",
                                user.email(), // PII, now in the log index
                                request.password(), // catastrophic
                                config.openaiKey()); // secret leak

                // SAFE, identifiers and correlation only
                log.error("login_failed userId={} correlationId={} reason={}",
                                user.id(),
                                MDC.get("requestId"),
                                "invalid_credentials"); // a generic code, not a DB message
        }
}

// Logs outlive the incident. They are shipped, indexed, and read by
// people who never touched this code. A password written once is a
// password in a search index for as long as retention lasts.
//
// One Java-specific trap: SLF4J treats a trailing Throwable
// specially.
// log.error("failed", e) -> prints the stack trace
// log.error("failed {}", e) -> treats e as a format argument
// and prints only its message
// The second loses the cause at exactly the moment you need it.
