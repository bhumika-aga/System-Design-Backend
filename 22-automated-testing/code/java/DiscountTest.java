// System Design - Backend
// Chapter 22, Automated Testing -> 03 Anatomy of a test
// Java 21 / JUnit 5 / AssertJ / Mockito / Testcontainers

package com.example.testing;

// Naming convention: Discount.java is tested by DiscountTest.java, in the
// same package under src/test/java. JUnit discovers @Test methods itself.
class DiscountTest {

    @Test
    void appliesPercentage() {
        // Arrange, set up inputs (and any doubles)
        Cart cart = new Cart(200);
        Coupon coupon = new Coupon(10);

        // Act, the ONE operation under test
        int total = Discount.apply(cart, coupon);

        // Assert, state the expectation. AssertJ prints expected AND actual
        // on failure, so the message explains itself.
        assertThat(total).isEqualTo(180);
    }
}
