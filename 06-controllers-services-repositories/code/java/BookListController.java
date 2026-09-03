// System Design - Backend
// Chapter 06, Controllers, Services & Repositories -> 05 Validate then transform
// Java 21 / Spring Boot 3.3

package com.example.layers.controller;

@RestController
@RequestMapping("/api/books")
class BookListController {
    
    @GetMapping
    List<Book> list(
        // VALIDATION: if it is present, it must be one of these.
        @RequestParam(required = false) @Pattern(regexp = "name|date", message = "sort must be 'name' or 'date'") String sort) {
        
        // TRANSFORMATION: query parameters are optional, so inject a
        // default here and no layer downstream ever sees an empty value.
        String order = (sort == null || sort.isBlank()) ? "date" : sort;
        
        return books.list(order); // delegate
    }
    // When the default is a constant rather than something computed,
    // Spring will do even this much for you:
    // @RequestParam(defaultValue = "date") String sort
}
