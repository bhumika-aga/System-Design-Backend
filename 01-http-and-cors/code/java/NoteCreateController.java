// System Design - Backend
// Chapter 01, HTTP & CORS -> MIME types: the Content-Type vocabulary
// Java 21 / Spring Boot 3.3

package com.example.notes;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** DTO. A record is immutable, and Jackson maps JSON straight onto it. */
record Note(Integer id, String title, boolean done) {
}

@RestController
@RequestMapping("/api/v1/notes")
class NoteController {

    // 1. Spring reads the body; Jackson parses it into a Note. A body that
    // isn't valid JSON never reaches here: Spring answers 400 itself.
    @PostMapping( // 2. headers decided FIRST
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Note> create(@RequestBody Note in) {
        Note saved = new Note(42, in.title(), in.done());

        return ResponseEntity
                .status(HttpStatus.CREATED) // 3. then the status line
                .body(saved); // 4. and the body LAST
    }
}
