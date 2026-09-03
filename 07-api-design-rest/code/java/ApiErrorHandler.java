// System Design - Backend
// Chapter 07, API Design (REST) -> 12 A consistent error envelope
// Java 21 / Spring Boot 3.3

package com.example.rest.errors;

record FieldIssue(String field, String issue) {
}

record ApiError(String code, String message,
                List<FieldIssue> details, String requestId) {
}

// ONE handler gives every error in the whole API the same shape.
@RestControllerAdvice
class ApiErrorHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
        // 422
    Map<String, ApiError> onValidationFailed(
        MethodArgumentNotValidException e) {
        
        List<FieldIssue> details = e.getBindingResult()
                                       .getFieldErrors()
                                       .stream()
                                       .map(f -> new FieldIssue(f.getField(),
                                           f.getDefaultMessage()))
                                       .toList();
        
        return Map.of("error", new ApiError(
            "validation_failed",
            "Some fields are invalid.",
            details,
            MDC.get("requestId")));
    }
}
// requestId comes from the MDC that the request-id filter set back in
// chapter 6, so a user can quote the id from an error response and you
// can find that exact request in the logs.
