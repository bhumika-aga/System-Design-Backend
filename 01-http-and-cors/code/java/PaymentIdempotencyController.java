// System Design - Backend
// Chapter 01, HTTP & CORS -> Implementation considerations
// Java 21 / Spring Boot 3.3

package com.example.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/payments")
class PaymentController {
    
    // key -> stored result. Use Redis with a TTL in production; a map leaks.
    private final Map<String, Receipt> seen = new ConcurrentHashMap<>();
    private final PaymentService payments;
    
    PaymentController(PaymentService payments) {
        this.payments = payments;
    }
    
    @PostMapping
    ResponseEntity<Receipt> create(
        @RequestHeader(value = "Idempotency-Key", required = false) String key,
        @RequestBody ChargeRequest request) {
        
        if (key == null || key.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "missing Idempotency-Key");
        }
        
        Receipt cached = seen.get(key);
        if (cached != null) { // replay: return the stored result
            return ResponseEntity.ok(cached);
        }
        
        Receipt result = payments.charge(request); // the real, unsafe work
        seen.put(key, result); // remember it BEFORE responding
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
// computeIfAbsent() would make the check-then-act atomic under a race.
