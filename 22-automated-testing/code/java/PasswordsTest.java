// System Design - Backend
// Chapter 22, Automated Testing -> 04 Unit tests
// Java 21 / JUnit 5 / AssertJ / Mockito / Testcontainers

package com.example.testing;

// The unit: pure logic, no I/O, trivially testable.
final class Passwords {

    static boolean isStrong(String p) {
        if (p.length() < 8) {
            return false;
        }
        boolean hasDigit = p.chars().anyMatch(Character::isDigit);
        boolean hasUpper = p.chars().anyMatch(Character::isUpperCase);
        return hasDigit && hasUpper;
    }
}

class PasswordsTest {

    @Test
    void rejectsPasswordsUnderEightCharacters() {
        assertThat(Passwords.isStrong("short1A")).isFalse();
    }

    @Test
    void acceptsAPasswordMeetingEveryRule() {
        assertThat(Passwords.isStrong("longEnough9")).isTrue();
    }
}

// Run: mvn test
// mvn test -Dtest=PasswordsTest (one class)
// mvn test -Dtest=PasswordsTest#rejects* (one method)
