// System Design - Backend
// Chapter 06, Controllers, Services & Repositories -> 07 The repository layer
// Java 21 / Spring Boot 3.3

package com.example.layers.repository;

// Spring Data derives the query from the method name, so for most
// of these the signature IS the implementation.
interface BookRepository extends JpaRepository<Book, Long> {

    // ALL books, in an order the caller chooses.
    List<Book> findAll(Sort sort);

    // A SEPARATE method for the single-book case. No optional flag
    // making one method quietly do two different jobs.
    Optional<Book> findById(Long id);

    // Anything the derivation cannot express, you write out -- with
    // a BOUND parameter, never string concatenation.
    @Query("select b from Book b where b.author = :author")
    List<Book> findByAuthor(@Param("author") String author);
}
// Sort.by(sort) is not the same as pasting `sort` into the SQL text.
// Spring Data resolves the name against the entity, so an unknown
// property is a startup-time error rather than an injection point.
