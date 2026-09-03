// System Design - Backend
// Chapter 22, Automated Testing -> 13 Fixtures & factories
// Java 21 / JUnit 5 / AssertJ / Mockito / Testcontainers

package com.example.testing;

class SignupTest {
    
    // A "fixture": JUnit creates this directory and deletes it afterwards.
    @TempDir
    Path dir;
    private Store store;
    
    // A "factory": a builder pre-loaded with sensible defaults, so each
    // test states only the field it actually cares about.
    private static User.Builder aUser() {
        return User.builder()
                   .id("u1")
                   .email("default@x.com")
                   .active(true);
    }
    
    @BeforeEach
    void openStore() {
        store = Store.open(dir);
    }
    
    @AfterEach
    void closeStore() {
        store.close(); // teardown runs even when the test fails
    }
    
    @Test
    void savesTheUser() {
        User u = aUser().email("ada@x.com").build(); // only what matters
        
        store.save(u);
        
        assertThat(store.findById("u1")).isPresent();
    }
}
