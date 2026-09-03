// System Design - Backend
// Chapter 01, HTTP & CORS -> How JWTs prevent tampering
// Java 21 / Spring Boot 3.3

package com.example.http;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@RestController
class AuthController {
    
    // Stateful: set a secure session cookie after login.
    @PostMapping("/login")
    ResponseEntity<Void> login(@RequestBody Credentials creds,
                               HttpSession session) {
        User user = users.authenticate(creds);
        session.setAttribute("userId", user.id()); // server-side store (Redis)
        
        ResponseCookie cookie = ResponseCookie.from("SESSION", session.getId())
                                    .httpOnly(true) // JS can't read it
                                    .secure(true) // HTTPS only
                                    .sameSite("Strict") // CSRF defense
                                    .maxAge(Duration.ofHours(1))
                                    .path("/")
                                    .build();
        
        return ResponseEntity.noContent()
                   .header(HttpHeaders.SET_COOKIE, cookie.toString())
                   .build();
    }
    
    // Stateless: a bearer-token filter, checked on every request.
}

@Component
class BearerTokenFilter extends OncePerRequestFilter {
    
    private final JwtVerifier jwt;
    
    BearerTokenFilter(JwtVerifier jwt) {
        this.jwt = jwt;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
        throws ServletException, IOException {
        
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")
                || !jwt.isValid(auth.substring(7))) {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED); // 401
            return;
        }
        chain.doFilter(request, response);
    }
}
