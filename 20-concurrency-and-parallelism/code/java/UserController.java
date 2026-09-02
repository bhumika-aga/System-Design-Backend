// System Design - Backend
// Chapter 20, Concurrency & Parallelism -> 09 Virtual Threads
// Java 21 / Spring Boot 3.3

package com.example.concurrency.vthreads;

@RestController
class UserController {

    private final JdbcClient jdbc;

    UserController(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/users/{id}")
    User get(@PathVariable long id) {

        // This call BLOCKS the virtual thread, but not the carrier
        // thread underneath it. The JVM unmounts this virtual thread
        // and runs another one on the same OS thread while we wait,
        // then remounts this one when the database answers, possibly
        // on a different carrier thread entirely.
        //
        // Note what is NOT here: no async, no await, no callback, no
        // CompletableFuture. Plain blocking code that scales anyway.
        return jdbc.sql("select name, email from users where id = :id")
                .param("id", id)
                .query(User.class)
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
