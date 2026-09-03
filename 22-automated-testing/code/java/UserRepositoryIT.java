// System Design - Backend
// Chapter 22, Automated Testing -> 09 Testing with a real database
// Java 21 / JUnit 5 / AssertJ / Mockito / Testcontainers

package com.example.testing;

// @Tag keeps slow integration tests out of the fast unit run. Surefire is
// configured to exclude the tag, and Failsafe to include it:
//   mvn test                              -> unit only
//   mvn verify -Dgroups=integration       -> these as well
@Tag("integration")
@SpringBootTest
@Testcontainers
class UserRepositoryIT {
    
    // A REAL Postgres in a throwaway container, started once per class and
    // torn down automatically when it finishes.
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");
    @Autowired
    UserRepository repo; // Flyway applies the production schema
    
    // Point Spring at the container instead of the usual datasource.
    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
    
    @Test
    void savesAndReadsBackThroughRealSql() {
        repo.save(new User("u1", "a@x.com")); // the REAL query path
        
        assertThat(repo.findById("u1"))
            .get()
            .extracting(User::email)
            .isEqualTo("a@x.com");
    }
}
