// System Design - Backend
// Chapter 01, HTTP & CORS -> The codes you'll actually use
// Java 21 / Spring Boot 3.3

package com.example.http;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
class UserLookupController {
    
    @GetMapping("/users/{id}")
    User get(@PathVariable String id,
             @AuthenticationPrincipal AppUser caller) {
        
        if (caller == null) { // 401: who are you?
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "login required");
        }
        
        User user = users.findById(id).orElseThrow( // 404: no such thing
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "no such user"));
        
        if (!user.visibleTo(caller)) { // 403: you, specifically, may not
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "forbidden");
        }
        
        return user; // 200
    }
}
