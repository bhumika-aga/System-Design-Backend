// System Design - Backend
// Chapter 07, API Design (REST) -> 05 Making POST retry-safe
// Java 21 / Spring Boot 3.3

package com.example.rest.idempotency;

@RestController
class IdempotentPaymentController {
    
    @PostMapping("/payments")
    ResponseEntity<Payment> create(
        @RequestHeader("Idempotency-Key") String key, // client UUID
        @RequestBody ChargeRequest body) {
        
        // Seen this exact request already? Return the SAME result rather
        // than charging the card a second time.
        Optional<StoredResult> prior = idempotency.find(key);
        if (prior.isPresent()) {
            return ResponseEntity.status(prior.get().status())
                       .body(prior.get().payment());
        }
        
        Payment payment = payments.charge(body.amount()); // side effect
        idempotency.save(key, HttpStatus.CREATED.value(), payment);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
    // The key must come from the client, not the server: it has to
    // survive the retry, and a server-generated one never would.
}
