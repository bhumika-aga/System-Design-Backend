// System Design - Backend
// Chapter 17, Backend Security -> 06 Cookies & flags
// Java 21 / Spring Boot 3.3 / Spring Security 6

package com.example.security.sessions;

class SessionCookiesDemo {
    
    void demo() throws Exception {
        ResponseCookie session = ResponseCookie.from("SESSION", sessionId)
                                     .httpOnly(true) // JavaScript cannot read it
                                     .secure(true) // HTTPS only
                                     .sameSite("Strict") // never sent cross-site
                                     .maxAge(Duration.ofDays(7))
                                     .path("/")
                                     .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, session.toString());
        
        // With Spring Session the flags are configuration, not code, which
        // means they cannot be forgotten on one endpoint out of twelve:
        //
        // server:
        // servlet:
        // session:
        // cookie:
        // http-only: true
        // secure: true
        // same-site: strict
        // max-age: 7d
    }
}
