// System Design - Backend
// Chapter 22, Automated Testing -> 05 Test doubles
// Java 21 / JUnit 5 / AssertJ / Mockito / Testcontainers

package com.example.testing;

// The unit depends on interfaces. That interface is the seam which lets
// us substitute a double (sec 6).
interface UserRepo {
    Optional<User> findById(String id);
}

interface Notifier {
    void send(String to, String message);
}

@Service
class Greeter {
    
    private final UserRepo repo;
    private final Notifier notifier;
    
    Greeter(UserRepo repo, Notifier notifier) {
        this.repo = repo;
        this.notifier = notifier;
    }
    
    void greet(String id) {
        User u = repo.findById(id).orElseThrow(); // collaborator 1
        notifier.send(u.email(), "Hi " + u.name()); // the interaction
    }
}

@ExtendWith(MockitoExtension.class)
class GreeterTest {
    
    @Mock
    UserRepo repo; // STUB: returns whatever we tell it to
    @Mock
    Notifier notifier; // SPY: records the calls it received
    @InjectMocks
    Greeter greeter;
    
    @Test
    void sendsOneGreetingToTheUser() {
        when(repo.findById("u1")) // arrange
            .thenReturn(Optional.of(new User("a@x.com", "Ada")));
        
        greeter.greet("u1"); // act
        
        // Assert the INTERACTION rather than any returned state.
        verify(notifier).send("a@x.com", "Hi Ada");
        verifyNoMoreInteractions(notifier);
    }
}
