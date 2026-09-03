// System Design - Backend
// Chapter 05, Validation & Transformation -> Syntactic validation
// Java 21 / Spring Boot 3.3 / Jakarta Bean Validation

package com.example.validation.syntax;

// Syntax asks "is it shaped like an email, a phone, a date?"
// Most of that is a built-in annotation; the rest is one regex.
record Contact(
    @NotBlank @Email(message = "invalid email format") String email,
    
    // optional +, then 7-15 digits
    @NotBlank @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "invalid phone number format") String phone,
    
    @NotNull LocalDate dateOfBirth) {
} // the TYPE does the date check

// "randomstring" -> email: invalid email format
// "2025-13-40" -> never becomes a LocalDate; rejected while parsing
//
// Choosing LocalDate over String is the cheapest validation in the
// chapter: an impossible date cannot survive being parsed.
