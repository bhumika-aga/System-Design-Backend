// System Design - Backend
// Chapter 15, Logging & Observability -> 11 The full LMO pattern
// Java 21 / Spring Boot 3.3 / Micrometer

package com.example.observability.service;

@Service
class TodoService {

    private static final Logger log = LoggerFactory.getLogger(TodoService.class);

    private final TodoRepository repo;
    private final Tracer tracer;

    TodoService(TodoRepository repo, Tracer tracer) {
        this.repo = repo;
        this.tracer = tracer;
    }

    // @NewSpan opens the child span. Its parent arrived with the
    // request, so no context has to be threaded through by hand.
    @NewSpan("TodoService.createTodo")
    Todo createTodo(CreateTodoRequest req, String userId) {

        Span span = tracer.currentSpan();
        if (span != null) {
            // business context, visible in the trace UI
            span.tag("user.id", userId);
            span.tag("todo.title", req.title());
            span.tag("todo.priority", req.priority());
        }

        // traceId is already in the MDC, so it lands on each of these
        // lines without being passed in.
        log.info("creating todo userId={} title={}", userId, req.title());

        try {
            Todo todo = repo.save(req.toEntity(userId));

            log.debug("todo created id={}", todo.id()); // dev only
            log.info("todo created successfully id={} userId={} priority={}",
                    todo.id(), userId, todo.priority());

            return todo;

        } catch (DataAccessException e) {
            // The exception OBJECT, not e.getMessage(): the stack
            // trace is the whole point here (ch 12).
            log.error("failed to create todo userId={}", userId, e);

            if (span != null) {
                span.error(e); // shows as failed in Jaeger / Tempo
            }
            throw e;
        }
    }
}
