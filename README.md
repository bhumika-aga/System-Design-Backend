# System Design - Backend

A 24-chapter field manual on backend engineering — from the HTTP request on the
wire, through databases, caching and queues, out to Kafka and WebSockets — with
every example written in **Java 21 and Spring Boot 3.3**, and a Python
counterpart alongside it.

Each chapter is a single self-contained HTML document: the explanation, the
diagrams, and the code in one place. Nothing to install, nothing to build.

## Reading it

Any static file server works. From the repository root:

```bash
python3 -m http.server 4173
```

Then open <http://localhost:4173>. Opening `index.html` straight from disk works
too, though progress tracking and saved notes behave better over HTTP.

Chapters stand alone. Read them in order for the full arc, or jump to the one
you need — each assumes only what came before it in the same chapter.

## Chapters

### Part I — The request

| #   | Chapter                                                                                     | What it covers                                                                                                                                                                     |
| --- | ------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 01  | [HTTP & CORS](01-http-and-cors/html_notes/notes.html)                                       | The protocol every backend speaks: methods and their semantics, headers, status codes, cookies, caching, conditional requests, and why a cross-origin call needs permission first. |
| 02  | [Routing](02-routing/html_notes/notes.html)                                                 | How a method plus a path finds the one handler meant to answer it — static and dynamic segments, path vs query parameters, nesting, versioning, catch-alls.                        |
| 03  | [Serialization](03-serialization/html_notes/notes.html)                                     | Turning objects into bytes and back. JSON and Protobuf, field mapping, and where an encoding choice quietly costs you.                                                             |
| 04  | [Authentication & Authorization](04-authentication-and-authorization/html_notes/notes.html) | Proving who a caller is, then deciding what they may do: sessions, JWTs, OAuth flows, and role-based access.                                                                       |
| 05  | [Validation & Transformation](05-validation-and-transformation/html_notes/notes.html)       | Rejecting bad input at the edge before it reaches your domain, and reshaping the good input that gets through.                                                                     |

### Part II — The shape of a service

| #   | Chapter                                                                                            | What it covers                                                                                                |
| --- | -------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------- |
| 06  | [Controllers, Services & Repositories](06-controllers-services-repositories/html_notes/notes.html) | The layers a request passes through, what belongs in each, and how request context travels along with it.     |
| 07  | [API Design (REST)](07-api-design-rest/html_notes/notes.html)                                      | Modelling resources, using status codes honestly, and handling pagination, versioning and idempotency.        |
| 08  | [Databases](08-databases/html_notes/notes.html)                                                    | Connections and pooling, transactions and isolation, indexing, and the queries that cost the most.            |
| 09  | [Caching](09-caching/html_notes/notes.html)                                                        | What to cache and where. Cache-aside, TTLs, eviction policies, and the invalidation problems that follow.     |
| 10  | [Task Queues & Background Jobs](10-task-queues-and-background-jobs/html_notes/notes.html)          | Moving slow work off the request path: brokers, workers, retries, and what to do with jobs that keep failing. |
| 11  | [Full-Text Search](11-full-text-search/html_notes/notes.html)                                      | Why a database `LIKE` isn't search. Inverted indexes, analyzers and relevance scoring with Elasticsearch.     |

### Part III — Running in production

| #   | Chapter                                                                                         | What it covers                                                                                            |
| --- | ----------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| 12  | [Error Handling & Fault Tolerance](12-error-handling-and-fault-tolerance/html_notes/notes.html) | Failing predictably instead of catastrophically: retries, timeouts, circuit breakers and fallbacks.       |
| 13  | [gRPC & Service Communication](13-grpc-and-service-communication/html_notes/notes.html)         | Contract-first calls between services — protobuf, streaming, and when RPC earns its place over REST.      |
| 14  | [Configuration Management](14-configuration-management/html_notes/notes.html)                   | Environments, profiles and secrets, and keeping every one of them out of your source tree.                |
| 15  | [Logging & Observability](15-logging-and-observability/html_notes/notes.html)                   | Structured logs, metrics and traces — and which one to reach for when production is on fire.              |
| 16  | [Graceful Shutdown](16-graceful-shutdown/html_notes/notes.html)                                 | What a shutdown signal actually means, and how to drain in-flight work so a deploy doesn't drop requests. |
| 17  | [Backend Security](17-backend-security/html_notes/notes.html)                                   | Injection, XSS, CSRF and transport security, and the headers that shut each of them down.                 |

### Part IV — Scale

| #   | Chapter                                                                                  | What it covers                                                                                                      |
| --- | ---------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| 18  | [Scaling & Performance, Part 1](18-scaling-and-performance-part-1/html_notes/notes.html) | Vertical vs horizontal scaling, load balancing, and splitting data across shards.                                   |
| 19  | [Scaling & Performance, Part 2](19-scaling-and-performance-part-2/html_notes/notes.html) | CDNs, rate limiting and connection pooling — and how to find the bottleneck before optimising the wrong thing.      |
| 20  | [Concurrency & Parallelism](20-concurrency-and-parallelism/html_notes/notes.html)        | IO-bound vs CPU-bound work, what a thread really costs, and where virtual threads change the arithmetic.            |
| 21  | [Docker, Kubernetes & CI/CD](21-docker-kubernetes-and-cicd/html_notes/notes.html)        | Packaging an application so it runs the same everywhere, shipping it repeatably, and what the cluster does with it. |
| 22  | [Automated Testing](22-automated-testing/html_notes/notes.html)                          | Unit, integration and end-to-end tests — what each one is good for, and how much of each is worth writing.          |
| 23  | [Message Brokers & Kafka](23-message-brokers-and-kafka/html_notes/notes.html)            | Topics, partitions, consumer groups and offsets, and how event-driven design changes a system's shape.              |
| 24  | [WebSockets & Real-Time](24-websockets-and-realtime/html_notes/notes.html)               | Holding a connection open both ways: the upgrade handshake, and pushing updates instead of polling for them.        |

## How to read a chapter

Every chapter follows the same shape, so you always know where you are.

**Numbered sections** down the left, with the one you're reading highlighted.
**Diagrams** wherever a picture beats a paragraph. **Code cards** with a Java tab
and a Python tab — Java is the default; switch to Python to see the same idea in
a language that says it differently. Callouts carry the three things worth
pulling out of a page: _the one takeaway_, _a note_, and _a gotcha_ that will
cost you an afternoon if you miss it.

You can also **highlight any passage** to attach a note to it, **mark chapters
complete** on the home page, and **switch themes** from the dock in the corner.
All of that is stored in your own browser and never leaves it.

## Layout

```txt
NN-chapter-slug/
  html_notes/notes.html     the chapter itself
  code/java/                the Java examples, extracted as files
  code/python/              the Python counterparts
  code/{sql,yaml,...}/      supporting files where a chapter uses them
assets/                     shared CSS and JS
index.html                  the chapter index
```

## Notes on the code

Java 21, Spring Boot 3.3, Maven. Constructor injection with final fields,
records for DTOs, `ResponseEntity` wherever a status or header matters.

The examples teach a shape rather than forming a runnable project — they lean on
illustrative types like `NoteRepository` and `PaymentService` that aren't
defined here. Copy them into a Spring Boot project to run them; the standalone
examples, like the router in chapter 2, compile and run on their own.

---

"Learn the fundamentals, and the frameworks become obvious."

---

Written and maintained by **[@bhumika-aga](https://github.com/bhumika-aga)**.
