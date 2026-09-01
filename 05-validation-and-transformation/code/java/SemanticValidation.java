// System Design - Backend
// Chapter 05, Validation & Transformation -> Semantic validation
// Java 21 / Spring Boot 3.3 / Jakarta Bean Validation

package com.example.validation.semantics;

// Type and syntax cannot express "not in the future". Semantics
// need real logic checked against the real world -- here, the clock.
record Profile(
                @NotNull @Past(message = "date of birth cannot be in the future") LocalDate dateOfBirth,

                @NotNull @Min(value = 1, message = "must be at least 1") @Max(value = 120, message = "must be 120 or less") Integer age) {
}

// {"dateOfBirth":"2026-06-12"} -> cannot be in the future
// {"age":430} -> age: must be 120 or less
//
// @Past and @Future are the rare semantic rules that ship in the box
// precisely because "now" is universal. Anything richer -- "this SKU
// exists", "this slot is still free" -- needs a custom
// ConstraintValidator, or belongs in the service layer entirely.
