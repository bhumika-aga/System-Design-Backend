// System Design - Backend
// Chapter 12, Error Handling & Fault Tolerance -> 12 The canonical error type
// Java 21 / Spring Boot 3.3

package com.example.errors.errors;

// The canonical error type. `message` is safe to show a user;
// `cause` is for the log and must never reach the response.
class AppException extends RuntimeException {
    
    private final HttpStatus status;
    private final Object details; // field errors, for a 400
    
    private AppException(HttpStatus status, String message,
                         Object details, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.details = details;
    }
    
    // Factory methods, named for what actually went wrong.
    static AppException notFound(String resource) {
        return new AppException(HttpStatus.NOT_FOUND,
            resource + " not found", null, null);
    }
    
    static AppException conflict(String message) {
        return new AppException(HttpStatus.CONFLICT, message, null, null);
    }
    
    static AppException badRequest(String message, Object details) {
        return new AppException(HttpStatus.BAD_REQUEST, message,
            details, null);
    }
    
    static AppException internal(Throwable cause) {
        // NEVER put cause.getMessage() into the user-facing message
        return new AppException(HttpStatus.INTERNAL_SERVER_ERROR,
            "something went wrong", null, cause);
    }
    
    HttpStatus status() {
        return status;
    }
    
    Object details() {
        return details;
    }
}
// Extending RuntimeException, not Exception, is deliberate: a checked
// exception would force every layer to declare or wrap it, and the
// layers in between have nothing useful to add.
