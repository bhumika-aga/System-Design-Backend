// System Design - Backend
// Chapter 12, Error Handling & Fault Tolerance -> 12 The global error handler
// Java 21 / Spring Boot 3.3

package com.example.errors.errors;

// One @RestControllerAdvice IS the global handler. Spring routes
// every exception from every controller through it.
@RestControllerAdvice
class GlobalErrorHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);
    
    @ExceptionHandler(AppException.class)
    ResponseEntity<ErrorResponse> onAppException(AppException e) {
        if (e.getCause() != null) {
            log.error("app error", e.getCause()); // logged, not sent
        }
        return respond(e.status(), e.getMessage(), e.details());
    }
    
    // A unique-constraint violation is a 409, not a 500.
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> onIntegrity(
        DataIntegrityViolationException e) {
        log.warn("integrity violation", e);
        return respond(HttpStatus.CONFLICT,
            "resource already exists", null);
    }
    
    // Bean Validation failures already carry the field errors.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> onInvalid(
        MethodArgumentNotValidException e) {
        
        List<String> fields = e.getBindingResult().getFieldErrors()
                                  .stream()
                                  .map(f -> f.getField() + ": " + f.getDefaultMessage())
                                  .toList();
        
        return respond(HttpStatus.BAD_REQUEST, "validation failed", fields);
    }
    
    // The final safety net. Anything unrecognised becomes a 500 with
    // a generic message; the stack trace goes to the log only.
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> onAnythingElse(Exception e) {
        log.error("unhandled exception", e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR,
            "something went wrong", null);
    }
    
    private ResponseEntity<ErrorResponse> respond(
        HttpStatus status, String message, Object details) {
        return ResponseEntity.status(status)
                   .body(new ErrorResponse(status.value(), message, details));
    }
    
    record ErrorResponse(int code, String message, Object details) {
    }
}
// You do not order these by hand. Spring picks the handler whose
// exception type is nearest to the one thrown, so the Exception
// catch-all never shadows the specific handlers above it.
