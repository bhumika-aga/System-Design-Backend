// System Design - Backend
// Chapter 05, Validation & Transformation -> The validation pipeline
// Java 21 / Spring Boot 3.3 / Jakarta Bean Validation

package com.example.validation.pipeline;

// The DTO *is* the schema. Each annotation declares one rule:
//   @NotBlank -> the existence check
//   the type  -> the type check (String)
//   @Size     -> the constraint check
record CreateBook(
        @NotBlank(message = "is required") @Size(min = 5, max = 100, message = "must be 5 to 100 characters") String name) {
}

@RestController
@RequestMapping("/api/books")
class BookController {

    // @Valid runs the whole pipeline before the method body starts.
    // A JSON number for `name` never even reaches validation: Jackson
    // fails the type check while parsing, and Spring answers 400.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Book create(@Valid @RequestBody CreateBook body) {
        return books.create(body.name()); // only valid data gets here
    }
}

// One place turns failures into 400-ready messages, instead of every
// controller formatting its own.
@RestControllerAdvice
class ValidationErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, List<String>> onInvalid(
            MethodArgumentNotValidException e) {

        List<String> messages = e.getBindingResult().getFieldErrors()
                .stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();

        return Map.of("errors", messages);
    }
}
