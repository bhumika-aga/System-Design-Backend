// System Design - Backend
// Chapter 04, Authentication & Authorization -> 16 Authorization, ABAC
// Java 21 / Spring Boot 3.3 / Spring Security 6

@RestController
class NoteEditController {
    
    // ABAC: the decision depends on attributes of the subject, the
    // resource and the environment -- not on a role alone.
    @Component("notePolicy")
    
    // Spring can call that bean straight from the annotation, which keeps
    // the rule in one place instead of copied into every handler.
    @PreAuthorize("@notePolicy.canEdit(principal, #note, "
                      + "T(java.time.LocalTime).now().getHour())")
    @PutMapping("/notes/{id}")
    void edit(@PathVariable String id, @RequestBody Note note) {
        notes.save(note);
    }
}

class NotePolicy {
    
    boolean canEdit(Subject subject, Note note, int hour) {
        if (note.archived()) {
            return false;
        }
        
        boolean owns = subject.id().equals(note.ownerId());
        boolean sameDept = subject.dept().equals(note.dept());
        boolean businessHours = hour >= 9 && hour < 18;
        
        // the owner, or an editor in the same department, and only
        // during business hours
        return (owns || (sameDept && "editor".equals(subject.role())))
                   && businessHours;
    }
}
