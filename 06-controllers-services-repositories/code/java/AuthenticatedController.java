// System Design - Backend
// Chapter 06, Controllers, Services & Repositories -> 14 Passing authentication data
// Java 21 / Spring Boot 3.3

package com.example.layers.controller;

@RestController
@RequestMapping("/api/books")
class AuthenticatedController {
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Book create(@Valid @RequestBody CreateBookRequest req,
                @AuthenticationPrincipal AppUser caller) {
        
        // The identity arrives as a parameter, already verified by the
        // filter. It is never read from the request body: a client can
        // put any ownerId it likes in there.
        if (caller == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        
        // Persist with the SERVER-VERIFIED owner, never the client's.
        return books.create(req, caller.id());
    }
    // Spring resolves @AuthenticationPrincipal out of the
    // SecurityContext, so this is type-safe. Compare that with pulling a
    // key from a map and casting the result, where a typo compiles
    // perfectly and fails only at runtime.
}
