// System Design - Backend
// Chapter 05, Validation & Transformation -> Complex, cross-field rules
// Java 21 / Spring Boot 3.3 / Jakarta Bean Validation

package com.example.validation.crossfield;

record Signup(
    @NotBlank @Size(min = 8, message = "must be at least 8 characters") String password,
    
    @NotBlank String passwordConfirmation,
    
    @NotNull Boolean married,
    
    String partner) { // required only when married
    
    // A field annotation can only ever see its own value, so a
    // cross-field rule needs somewhere else to live. @AssertTrue on a
    // derived getter is the simplest home: Jakarta calls it like any
    // other constraint, and the method name becomes the error's field.
    @AssertTrue(message = "passwords don't match")
    boolean isPasswordConfirmed() {
        return password != null && password.equals(passwordConfirmation);
    }
    
    @AssertTrue(message = "partner is required when married is true")
    boolean isPartnerPresentWhenMarried() {
        return !Boolean.TRUE.equals(married)
                   || (partner != null && !partner.isBlank());
    }
}
