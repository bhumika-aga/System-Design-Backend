// System Design - Backend
// Chapter 04, Authentication & Authorization -> 16 Authorization, RBAC
// Java 21 / Spring Boot 3.3 / Spring Security 6

// RBAC in Spring is an annotation, evaluated after authentication
// has populated the security context.
@Configuration
@EnableMethodSecurity // required for @PreAuthorize to run
class MethodSecurityConfig {
}

@RestController
class AdminController {
    
    @PreAuthorize("hasRole('ADMIN')") // 403 for everyone else
    @GetMapping("/admin/deadzone")
    String deadZone() {
        return "admins only";
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PostMapping("/notes/{id}/publish")
    void publish(@PathVariable String id) {
        notes.publish(id);
    }
}
// Roles carry a ROLE_ prefix by convention. hasRole('ADMIN') adds it
// for you; hasAuthority('ADMIN') does not. Mixing the two is the
// classic reason a rule silently never matches.
