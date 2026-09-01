// System Design - Backend
// Chapter 12, Error Handling & Fault Tolerance -> 10 Retry with backoff
// Java 21 / Spring Boot 3.3

package com.example.errors.retry;

@Service
class EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

    // Spring Retry turns the whole loop into an annotation.
    @Retryable(retryFor = TransientEmailException.class, // and nothing else
            maxAttempts = 5, backoff = @Backoff(delay = 1000, multiplier = 2, random = true))
    public void send(String to, String subject, String body) {
        emailClient.send(to, subject, body);
    }

    // Called once the attempts are exhausted. Without it, the caller
    // simply sees the last exception and the message is lost.
    @Recover
    void giveUp(TransientEmailException e,
            String to, String subject, String body) {
        log.error("all retries exhausted for {}", to, e);
        deadLetters.save(to, subject, body);
    }
}
// delay=1000, multiplier=2 gives 1s, 2s, 4s, 8s, 16s.
//
// random=true is the jitter, and it is not decoration: without it
// every instance that failed at the same moment retries at the same
// moment, and the herd arrives together.
//
// WHICH exceptions retry is a decision, not a default. A 429 or a
// 503 is worth retrying. A 400 or a 401 will fail identically every
// time, so retrying it only burns the budget before the one error
// that might have succeeded.
