// System Design - Backend
// Chapter 22, Automated Testing -> 07 Table-driven & parametrized tests
// Java 21 / JUnit 5 / AssertJ / Mockito / Testcontainers

package com.example.testing;

class DiscountTableTest {

    // The TABLE: each row is one case, and the first column names it, so a
    // failure report tells you which row broke rather than which line.
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "ten percent off 200, 200,  10, 180",
            "zero discount,       100,   0, 100",
            "full discount,       100, 100,   0",
            "rounds down,          99,  10,  90" // boundary case
    })
    void appliesDiscount(String name, int subtotal, int percent, int want) {
        int got = Discount.apply(new Cart(subtotal), new Coupon(percent));

        assertThat(got).as(name).isEqualTo(want);
    }
}
// @ValueSource, @EnumSource and @MethodSource cover the other shapes:
// @MethodSource is the one to reach for when a row needs real objects.
