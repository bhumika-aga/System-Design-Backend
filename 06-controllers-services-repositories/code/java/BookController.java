// System Design - Backend
// Chapter 06, Controllers, Services & Repositories -> 05 The controller layer
// Java 21 / Spring Boot 3.3

package com.example.layers.controller;

// The DTO is the bind target. Jackson turns the JSON body into it.
record CreateBookRequest(String title, String author) {
}

@RestController
@RequestMapping("/api/books")
class BookController {
    
    private final BookService books;
    
    BookController(BookService books) {
        this.books = books;
    }
    
    // Steps 1 and 2 -- extract the body, then bind it -- happen
    // before this method runs. A malformed payload never reaches the
    // code below: Jackson fails, Spring raises
    // HttpMessageNotReadableException, and one @RestControllerAdvice
    // turns that into a 400.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Book create(@Valid @RequestBody CreateBookRequest req) {
        // validation has already run too; delegation follows
        return books.create(req);
    }
}
