// System Design - Backend
// Chapter 01, HTTP & CORS -> The optimistic-locking flow
// Java 21 / Spring Boot 3.3

package com.example.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
class OptimisticLockingController {

    @PutMapping("/docs/{id}")
    ResponseEntity<Void> update(
            @PathVariable String id,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestBody DocPatch patch) {

        Doc doc = docs.get(id);

        if (ifMatch == null) { // the ETag the client last saw
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "If-Match required");
        }
        if (!ifMatch.equals(doc.etag())) { // someone changed it first
            throw new ResponseStatusException(
                    HttpStatus.PRECONDITION_FAILED, "version conflict"); // 412
        }

        Doc updated = docs.save(doc.apply(patch)); // saving bumps the version
        return ResponseEntity.ok().eTag(updated.etag()).build();
    }
    // With JPA, an @Version column runs this same check inside the database
    // and throws OptimisticLockingFailureException, which you map to 412.
}
