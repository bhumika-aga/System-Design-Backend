// System Design - Backend
// Chapter 05, Validation & Transformation -> Transform then validate
// Java 21 / Spring Boot 3.3 / Jakarta Bean Validation

package com.example.validation.pagination;

// Query parameters are always strings on the wire. Spring casts
// them for you, so here the transform happens BEFORE validation
// rather than being something you write.
@Validated // required for parameter constraints
@RestController
class BookQueryController {
    
    @GetMapping("/api/books")
    List<Book> list(
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "must be 1 or more") @Max(value = 499, message = "must be 499 or less") int page,
        
        @RequestParam(defaultValue = "20") @Min(1) @Max(9999) int limit) {
        
        return books.page(page, limit);
    }
}
// ?page=abc -> the cast fails first
// (MethodArgumentTypeMismatchException)
// ?page=0 -> the cast succeeds, then the constraint rejects it
//
// @Validated on the class is what makes parameter constraints run.
// On a @RequestBody, @Valid on the parameter is enough.
