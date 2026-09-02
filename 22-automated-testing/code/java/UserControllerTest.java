// System Design - Backend
// Chapter 22, Automated Testing -> 10 Testing HTTP handlers & APIs
// Java 21 / JUnit 5 / AssertJ / Mockito / Testcontainers

package com.example.testing;

// @WebMvcTest boots ONLY the web layer: the real routing, filters, JSON
// mapping and validation, with no server socket and no database.
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mvc;
    @MockitoBean
    UserService users; // the collaborator is a double (sec 6)

    @Test
    void createUserReturns201() throws Exception {
        // Arrange + Act: drive the controller in-process, no network.
        mvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"a@x.com","name":"Ada"}
                        """))
                // Assert on the real response: status first, then the body.
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("a@x.com"));
    }
}
// For the full stack including the servlet container, swap in
// @SpringBootTest(webEnvironment = RANDOM_PORT) and TestRestTemplate.
