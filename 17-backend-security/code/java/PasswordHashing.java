// System Design - Backend
// Chapter 17, Backend Security -> 05 Password storage
// Java 21 / Spring Boot 3.3 / Spring Security 6

package com.example.security.passwords;

@Configuration
class PasswordConfig {

    // Spring Security ships the encoder; you do not implement Argon2.
    @Bean
    PasswordEncoder passwordEncoder() {
        // saltLength, hashLength, parallelism, memoryKb, iterations
        return new Argon2PasswordEncoder(16, 32, 1, 64 * 1024, 4);
    }
}

class PasswordConfigDemo {

    void demo() throws Exception {

        // Hashing at signup
        String stored = encoder.encode(rawPassword);

        // -> $argon2id$v=19$m=65536,t=4,p=1$<salt>$<hash>
        // the algorithm, its parameters AND the salt travel with the hash,
        // which is what lets you raise the cost later without a migration.

        // Verifying at login
        boolean ok = encoder.matches(rawPassword, stored);

        // Two things this hands you for free:
        // * the salt is generated per password and stored for you -- there
        // is no salt column to forget
        // * matches() compares in constant time, so an attacker learns
        // nothing from how long the comparison took
        //
        // DelegatingPasswordEncoder goes further: it reads the {id} prefix on
        // each stored hash, so a database holding old bcrypt hashes and new
        // Argon2 hashes keeps working while you re-hash on next login.
    }
}
