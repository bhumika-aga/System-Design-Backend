// System Design - Backend
// Chapter 07, API Design (REST) -> 15 Worked code: a Task API in Java
// Java 21 / Spring Boot 3.3

package com.example.rest.taskapi;

// A REST API in Java and Spring Boot for managing "tasks".
//
// It applies three design rules from this chapter:
//   sec 3  URL versioning        -> every route lives under /v1
//   sec 6  List query parameters -> pagination + sorting + filtering
//   sec 8  Consistent naming     -> camelCase JSON, with sane defaults
//
// Run:  ./mvnw spring-boot:run
// Try:  curl "localhost:8080/v1/tasks?size=5&sort=title,asc&done=false"

// ---------- Model ----------

// Spring Data writes the implementation. Pagination, sorting and the
// count query all come from the Pageable argument.
interface TaskRepository extends JpaRepository<Task, Long> {
    
    // Filtering, derived from the method name alone.
    Page<Task> findByDone(boolean done, Pageable pageable);
    
    Page<Task> findByTitleContainingIgnoreCase(String title,
                                               Pageable pageable);
}

// ---------- Repository ----------

// Java field names are already camelCase, so the wire format matches
// them with no annotations at all: createdAt, never created_at.
@Entity
class Task {
    
    @Id
    @GeneratedValue
    Long id;
    
    String title;
    boolean done;
    Instant createdAt = Instant.now();
    
    // accessors omitted for brevity
}

// ---------- The list envelope ----------

// Wrapping the array in an object gives pagination metadata a place
// to live beside the data.
record Pagination(int page, int size, long totalItems, int totalPages) {
    
    static Pagination of(Page<?> p) {
        return new Pagination(p.getNumber(), p.getSize(),
            p.getTotalElements(), p.getTotalPages());
    }
}

record ListResponse<T>(List<T> data, Pagination pagination) {
}

record CreateTask(@NotBlank String title) {
}

record UpdateTask(@NotBlank String title, boolean done) {
}

// ---------- Controller ----------

@RestController
@RequestMapping("/v1/tasks") // sec 3: versioned, so /v2 can differ
class TaskController {
    
    // an allowlist: clients sort by fields we chose, never by one
    // they invented
    private static final Set<String> SORTABLE = Set.of("id", "title", "createdAt");
    
    private final TaskRepository tasks;
    
    TaskController(TaskRepository tasks) {
        this.tasks = tasks;
    }
    
    @GetMapping
    ListResponse<Task> list(
        @RequestParam(required = false) Boolean done,
        @RequestParam(required = false) String title,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        rejectUnsortableFields(pageable.getSort());
        
        Page<Task> page;
        if (done != null) {
            page = tasks.findByDone(done, pageable);
        } else if (title != null && !title.isBlank()) {
            page = tasks.findByTitleContainingIgnoreCase(title, pageable);
        } else {
            page = tasks.findAll(pageable);
        }
        
        // A page past the end is an EMPTY list and still 200, never a
        // 404: an empty result is not a missing resource (sec 7).
        return new ListResponse<>(page.getContent(), Pagination.of(page));
    }
    
    private void rejectUnsortableFields(Sort sort) {
        sort.forEach(order -> {
            if (!SORTABLE.contains(order.getProperty())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "sort must be one of: id, title, createdAt");
            }
        });
    }
    
    @GetMapping("/{id}")
    Task get(@PathVariable Long id) {
        return tasks.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "task not found"));
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Task create(@Valid @RequestBody CreateTask body) {
        Task task = new Task();
        task.title = body.title();
        return tasks.save(task);
    }
    
    @PutMapping("/{id}")
    Task update(@PathVariable Long id,
                @Valid @RequestBody UpdateTask body) {
        Task task = get(id);
        task.title = body.title();
        task.done = body.done();
        return tasks.save(task);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        if (!tasks.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "task not found");
        }
        tasks.deleteById(id);
    }
}

// ---------- Health ----------

// Unversioned on purpose: infrastructure, not part of the versioned
// resource API. Spring Boot Actuator already ships /actuator/health.
@RestController
class HealthController {
    
    @GetMapping("/healthz")
    Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
