// System Design - Backend
// Chapter 11, Full-Text Search -> 09 Postgres full-text search in Java
// Java 21 / Spring Boot 3.3 / JdbcClient

package com.example.search;

// The row the query returns. A record maps straight onto it.
record ProductHit(long id, String name, String description, double rank) {
}

@RestController
class SearchController {

    // plainto_tsquery converts plain text safely: no special characters
    // to escape. websearch_to_tsquery is the richer sibling, supporting
    // AND / OR / -term the way a search box user expects.
    private static final String SEARCH_SQL = """
            SELECT id, name, description,
                   ts_rank(search_vec,
                           plainto_tsquery('english', :q)) AS rank
            FROM   products
            WHERE  search_vec @@ plainto_tsquery('english', :q)
            ORDER  BY rank DESC
            LIMIT  20
            """;

    private final JdbcClient jdbc;

    SearchController(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/search")
    List<ProductHit> search(
            @RequestParam("q") @NotBlank(message = "missing query param q") String query) {

        return jdbc.sql(SEARCH_SQL)
                .param("q", query) // bound, so a quote is just a quote
                .query(ProductHit.class)
                .list();
    }
}
// The search_vec column and its GIN index are the previous snippet.
// Postgres does the ranking; this handler only asks the question.
//
// Two Java-specific notes:
// * a text block keeps the SQL readable and, more importantly,
// keeps it a single constant rather than concatenated fragments
// * :q appears twice but binds ONCE. Naming the parameter, rather
// than using positional ?, is what makes that work.
