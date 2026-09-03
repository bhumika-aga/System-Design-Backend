// System Design - Backend
// Chapter 01, HTTP & CORS -> Wiring methods to handlers
// Java 21 / Spring Boot 3.3

package com.example.http;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notes")
class NoteController {
    
    private final NoteRepository notes;
    
    // Constructor injection: no field @Autowired, and the field is final.
    NoteController(NoteRepository notes) {
        this.notes = notes;
    }
    
    @GetMapping
        // safe, cacheable read
    List<Note> list() {
        return notes.findAll();
    }
    
    // @GetMapping answers HEAD too: Spring runs the handler, drops the body.
    @GetMapping("/{id}")
    Note get(@PathVariable String id) { // built-in path params
        return notes.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    
    @PostMapping // create (server assigns the id)
    @ResponseStatus(HttpStatus.CREATED)
    Note create(@RequestBody Note in) {
        return notes.save(in);
    }
    
    @PutMapping("/{id}")
        // full replace (idempotent)
    Note replace(@PathVariable String id, @RequestBody Note in) {
        return notes.replace(id, in);
    }
    
    @PatchMapping("/{id}")
        // partial update
    Note patch(@PathVariable String id,
               @RequestBody Map<String, Object> changes) {
        return notes.applyChanges(id, changes);
    }
    
    @DeleteMapping("/{id}") // remove (idempotent)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable String id) {
        notes.deleteById(id);
    }
}
// Path matches but method doesn't -> Spring returns 405 Method Not Allowed.
