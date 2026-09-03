// System Design - Backend
// Chapter 01, HTTP & CORS -> Redirections
// Java 21 / Spring Boot 3.3

package com.example.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
class RedirectController {
    
    // Permanent move that must keep the method and body -> 308
    @RequestMapping("/user/{id}")
    ResponseEntity<Void> oldRoute(@PathVariable String id) {
        return ResponseEntity
                   .status(HttpStatus.PERMANENT_REDIRECT) // 308
                   .location(URI.create("/person/" + id))
                   .build();
    }
    
    // Post/Redirect/Get -> 303 so a browser refresh won't re-POST the form
    @PostMapping("/submit")
    ResponseEntity<Void> submitForm(@ModelAttribute FormData form) {
        String id = forms.save(form);
        return ResponseEntity
                   .status(HttpStatus.SEE_OTHER) // 303, forces a GET
                   .location(URI.create("/results/" + id))
                   .build();
    }
}
