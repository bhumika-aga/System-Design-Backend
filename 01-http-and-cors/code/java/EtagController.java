// System Design - Backend
// Chapter 01, HTTP & CORS -> Layer 2: validation via ETag & Last-Modified
// Java 21 / Spring Boot 3.3

package com.example.http;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
class EtagController {

    @GetMapping("/resource")
    ResponseEntity<byte[]> get(
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch)
            throws NoSuchAlgorithmException {

        byte[] body = resources.load();
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(body);

        // A fingerprint of the content, quoted as the spec requires.
        String etag = "\"" + HexFormat.of().formatHex(digest, 0, 8) + "\"";

        if (etag.equals(ifNoneMatch)) { // client already has this version
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .build(); // 304, no body, payload saved
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(Duration.ofSeconds(10)))
                .body(body); // 200 + body
    }
    // ShallowEtagHeaderFilter automates this, but only after rendering the
    // body: it saves bandwidth, never the work of producing the response.
}
