// System Design - Backend
// Chapter 05, Validation & Transformation -> The controller as a gate
// Java 21 / Spring Boot 3.3 / Jakarta Bean Validation

package com.example.validation.gate;

// ---- schema (the gate) ----
record CreateBook(
    @NotBlank(message = "is required") @Size(min = 5, max = 100) String name) {
}

record Book(Long id, String name) {
}

// ---- CONTROLLER: owns HTTP and the validation gate ----
@RestController
@RequestMapping("/api/books")
class BookController {
    
    private final BookService books;
    
    BookController(BookService books) {
        this.books = books;
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Book create(@Valid @RequestBody CreateBook body) {
        // === the gate has already closed behind us ===
        // If @Valid had failed, Spring would have thrown before
        // entering this method and the database would never have been
        // touched. Nothing below this line can see bad data.
        return books.create(body.name().strip());
    }
}

// ---- SERVICE: business logic, trusting its input ----
@Service
class BookService {
    
    private final BookRepository repository;
    
    BookService(BookRepository repository) {
        this.repository = repository;
    }
    
    Book create(String name) {
        return repository.save(new Book(null, name));
    }
}
// That is the whole point of a gate: a bad request becomes a 400 at
// the edge, not a confusing 500 from a constraint violation raised
// deep inside the database.
