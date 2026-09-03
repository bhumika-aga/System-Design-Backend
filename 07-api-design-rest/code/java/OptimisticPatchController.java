// System Design - Backend
// Chapter 07, API Design (REST) -> 05 Methods & idempotency
// Java 21 / Spring Boot 3.3

package com.example.rest.concurrency;

@RestController
@RequestMapping("/v1/organizations")
class OptimisticPatchController {
    
    @PatchMapping("/{id}")
    ResponseEntity<Organization> update(
        @PathVariable String id,
        @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
        @RequestBody Map<String, Object> patch) {
        
        Organization org = orgs.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "organization not found"));
        
        // optimistic concurrency: refuse a write based on a stale read
        if (ifMatch != null && !ifMatch.equals(org.etag())) {
            throw new ResponseStatusException(
                HttpStatus.PRECONDITION_FAILED,
                "resource changed; re-fetch and retry");
        }
        
        // PATCH merges the fields it was given; it does not replace
        Organization updated = orgs.merge(id, patch);
        
        return ResponseEntity.ok()
                   .eTag(updated.etag())
                   .body(updated); // 200 + the updated entity
    }
}
