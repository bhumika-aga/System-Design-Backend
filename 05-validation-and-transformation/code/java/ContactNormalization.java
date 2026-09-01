// System Design - Backend
// Chapter 05, Validation & Transformation -> Normalisation
// Java 21 / Spring Boot 3.3 / Jakarta Bean Validation

package com.example.validation.normalize;

// Normalisation runs AFTER validation passes and BEFORE the service
// layer sees anything. A record is immutable, so it hands back a new
// value instead of mutating in place.
record Contact(String email, String phone) {

    Contact normalized() {
        String cleanEmail = email.strip().toLowerCase(Locale.ROOT);

        String cleanPhone = phone.strip();
        if (!cleanPhone.startsWith("+")) {
            cleanPhone = "+" + cleanPhone; // inject the missing +
        }

        return new Contact(cleanEmail, cleanPhone);
    }
}
// "Test@TEST.com" -> "test@test.com"
// "1234567" -> "+1234567"
//
// Locale.ROOT is not decoration. Under a Turkish locale
// "I".toLowerCase() returns a dotless i, and the same email would
// normalise to two different strings on two different servers.
