// System Design - Backend
// Chapter 22, Automated Testing -> 06 Dependency injection for testability
// Java 21 / JUnit 5 / AssertJ / Mockito / Testcontainers

package com.example.testing;

// Depend on an INTERFACE, not a concrete class. That interface is the seam.
interface OrderRepo {
    void save(Order order);
}

@Service
class OrderService {
    
    private final OrderRepo repo; // injected, never constructed inside
    
    OrderService(OrderRepo repo) {
        this.repo = repo;
    }
    
    void place(Order order) {
        if (order.total() <= 0) { // pure logic, easy to test
            throw new IllegalArgumentException("invalid total");
        }
        repo.save(order);
    }
}

class OrderServiceTest {
    
    @Test
    void rejectsANonPositiveTotal() {
        OrderService service = new OrderService(new InMemoryOrderRepo());
        
        assertThatThrownBy(() -> service.place(new Order(0)))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    // A FAKE: a real working implementation, just not the production one.
    static final class InMemoryOrderRepo implements OrderRepo {
        final List<Order> saved = new ArrayList<>();
        
        @Override
        public void save(Order order) {
            saved.add(order);
        }
    }
}
// In production Spring injects the JPA repository instead. Same class,
// real dependency, no test-only branches anywhere in it.
