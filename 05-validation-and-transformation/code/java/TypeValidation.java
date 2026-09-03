// System Design - Backend
// Chapter 05, Validation & Transformation -> Type validation
// Java 21 / Spring Boot 3.3 / Jakarta Bean Validation

package com.example.validation.types;

// Boxed types, not primitives. A `boolean` field silently defaults
// to false, so "absent" and "false" become indistinguishable;
// `Boolean` stays null when absent and @NotNull can catch it.
record TypePayload(
    @NotBlank String stringField,
    @NotNull Double numberField,
    @NotEmpty List<@NotBlank String> arrayField, // and each item
    @NotNull Boolean boolField) {
}

// Reject stray keys rather than ignoring them. application.yml:
// spring:
// jackson:
// deserialization:
// fail-on-unknown-properties: true

// Jackson enforces the base types while parsing, before any
// constraint runs:
// {"numberField":"x"} -> cannot deserialize Double from String
// {"arrayField":[1,2]} -> cannot deserialize String from Integer
//
// Both arrive as HttpMessageNotReadableException, which you map to a
// 400 in the same @RestControllerAdvice.
