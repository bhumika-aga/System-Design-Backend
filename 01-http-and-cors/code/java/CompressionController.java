// System Design - Backend
// Chapter 01, HTTP & CORS -> Compression: the same negotiation, applied to size
// Java 21 / Spring Boot 3.3

package com.example.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
class CompressionController {

    // The clean way is configuration, not code.
    // src/main/resources/application.yml:
    //
    // server:
    // compression:
    // enabled: true
    // mime-types: application/json,text/html,text/css
    // min-response-size: 1KB
    //
    // Tomcat then reads Accept-Encoding and sets Content-Encoding: gzip.

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Payload> get() {
        return ResponseEntity.ok()
                // tell caches the body varies by encoding
                .varyBy(HttpHeaders.ACCEPT_ENCODING)
                .body(payloads.big());
    }
    // Without Vary, a shared cache can hand a gzipped body to a client that
    // never asked for one and cannot decode it.
}
