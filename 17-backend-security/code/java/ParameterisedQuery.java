// System Design - Backend
// Chapter 17, Backend Security -> 03 SQL injection
// Java 21 / Spring Boot 3.3 / Spring Security 6

package com.example.security.injection;

@Repository
interface UserLookup extends Repository<User, Long> {

    // And a hand-written JPQL query still binds, as long as you name the
    // parameter rather than splicing it in:
    @Query("select u from User u where u.email = :email")
    Optional<User> lookup(@Param("email") String email);
}

class UserLookupDemo {

    void demo() throws Exception {
        // NEVER, string concatenation
        String query = "SELECT * FROM users WHERE email = '" + userInput + "'";

        // ALWAYS, a bound parameter: the value travels beside the SQL, never
        // inside it, so a quote in userInput stays a quote.
        User user = jdbc.sql("SELECT id, name FROM users WHERE email = :email")
                .param("email", userInput)
                .query(User.class)
                .single();

        // With Spring Data JPA, derived queries bind automatically:
        Optional<User> found = users.findByEmail(userInput);
    }
}
