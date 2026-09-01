// System Design - Backend
// Chapter 08, Databases -> 04 Parameter-safe queries
// Java 21 / Spring Boot 3.3 / JdbcClient

package com.example.db.query;

@Repository
class UserLookupRepository {

    private final JdbcClient jdbc;

    UserLookupRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // JdbcClient (Spring 6.1+). The SQL text and the value travel
    // separately, so userId is data and can never become SQL.
    User user = jdbc.sql("""
            SELECT u.*, to_jsonb(up.*) AS profile
            FROM users u
            LEFT JOIN user_profiles up ON u.id = up.user_id
            WHERE u.id = :id
            """)
            .param("id", userId) // bound, never spliced into the text
            .query(User.class)
            .single();

    // With Spring Data JPA the same lookup is just
    // users.findById(userId) and the SQL is generated for you. Reach for
    // JdbcClient when you want the query to be yours -- as here, where
    // the LEFT JOIN and to_jsonb are the point.
}
