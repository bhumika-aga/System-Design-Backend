// System Design - Backend
// Chapter 06, Controllers, Services & Repositories -> 06 The service layer
// Java 21 / Spring Boot 3.3

package com.example.layers.service;

// Notice what is absent: no HttpServletRequest, no ResponseEntity,
// no status codes. Nothing in these signatures says "API", and that
// is the point -- a scheduled job could call the same method.
@Service
class BookService {
    
    private final BookRepository repository;
    private final Mailer mailer;
    
    BookService(BookRepository repository, Mailer mailer) {
        this.repository = repository;
        this.mailer = mailer;
    }
    
    List<Book> list(String sort) {
        // Orchestration: ask the repository for what it needs.
        return repository.findAll(Sort.by(sort));
        // could also enrich, merge other repositories, notify...
    }
    
    // A service method that touches no database at all is still a
    // perfectly good service method.
    void notifyOwner(String email) {
        mailer.send(email, "Your book was added");
    }
}
