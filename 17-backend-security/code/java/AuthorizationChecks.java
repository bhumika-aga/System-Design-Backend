// System Design - Backend
// Chapter 17, Backend Security -> 09 BOLA & BFLA
// Java 21 / Spring Boot 3.3 / Spring Security 6

package com.example.security.authorization;

@RestController
class InvoiceController {
    
    // The check is an annotation, evaluated before the method runs.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/invoices")
    List<Invoice> adminInvoices() {
        return invoices.findAll();
    }
    
    // Needs @EnableMethodSecurity on a @Configuration class, otherwise
    // the annotation is silently ignored -- a failure mode worth knowing,
    // because the endpoint then looks protected and is not.
    
    // This is BFLA: can this user call this FUNCTION at all. BOLA is the
    // other half, and no annotation can answer it, because it depends on
    // the row: does invoice 5 belong to the caller?
    @GetMapping("/invoices/{id}")
    Invoice get(@PathVariable long id,
                @AuthenticationPrincipal AppUser caller) {
        
        // Scope the QUERY by owner. Fetching first and comparing after
        // still leaks existence through timing and error codes.
        return invoices.findByIdAndOwnerId(id, caller.id())
                   .orElseThrow(() -> new ResponseStatusException(
                       HttpStatus.NOT_FOUND));
    }
}
