// System Design - Backend
// Chapter 02, Routing -> A Router in Java (OOP)
//
// A router is a small object-oriented dispatch problem: a table from
// "method + route" to a handler. Building one by hand is the fastest
// way to see all four OOP pillars doing real work.
//
// Runs on its own:  javac RouterDemo.java && java RouterDemo

import java.util.LinkedHashMap;
import java.util.Map;

// ==================== INTERFACE (ABSTRACTION) ====================

// Handler is an INTERFACE -- a contract, a promise.
// It says: "any class with a handle(Request) method returning a
// Response IS a Handler."
//
// The Router only ever talks to this interface. It does not care
// whether the object is a GetUser, a ListBooks, or something written
// next year: if it satisfies the contract, the Router can use it.
interface Handler {
    Response handle(Request req);
}

// ==================== DATA CARRIERS ====================

// What the client is asking for.
class Request {
    final String method; // GET, POST, ...
    final String path; // /api/users/123
    final Map<String, String> params; // from the path (:id -> "123")
    final Map<String, String> query; // from the query (?page=2)

    Request(String method, String path,
            Map<String, String> params, Map<String, String> query) {
        this.method = method;
        this.path = path;
        this.params = params;
        this.query = query;
    }
}

// What the server sends back.
class Response {
    final int status;
    final String body;

    Response(int status, String body) {
        this.status = status;
        this.body = body;
    }
}

// ==================== ROUTER (ENCAPSULATION) ====================

// The Router is the brain: it holds every registered route.
class Router {

    // ENCAPSULATION: `routes` is private, so no outside code can reach
    // in and corrupt the table. Callers go through register() and
    // dispatch(), which is the entire public surface.
    //
    // LinkedHashMap, not HashMap, because registration order is also
    // match order -- and order matters once patterns can overlap.
    private final Map<String, Handler> routes = new LinkedHashMap<>();

    // ---- register: adding a route ----
    // The key is method + " " + pattern, because a route is not a path
    // alone: GET /notes and POST /notes are two different routes that
    // run two different handlers.
    void register(String method, String pattern, Handler handler) {
        routes.put(method + " " + pattern, handler);
    }

    // ---- dispatch: finding and running the right handler ----
    // This is the core of routing. It walks the table, finds the first
    // route whose method and pattern both match, and runs its handler.
    //
    // POLYMORPHISM lives on the handle() call below. The same line runs
    // GetUser.handle() or ListBooks.handle() depending only on which
    // object is sitting in the table. The Router never asks which.
    Response dispatch(String method, String path,
            Map<String, String> query) {

        for (Map.Entry<String, Handler> entry : routes.entrySet()) {
            String[] key = entry.getKey().split(" ", 2);
            Map<String, String> params = match(key[1], path);

            if (key[0].equals(method) && params != null) {
                return entry.getValue()
                        .handle(new Request(method, path, params, query));
            }
        }

        // Nothing matched. Every router needs this fallback, or an
        // unknown URL would simply hang with no answer at all.
        return new Response(404, "route not found");
    }

    // ---- match: the URL comparison engine ----
    // Compares a pattern against a real path, segment by segment, and
    // collects any dynamic values it finds.
    //
    // pattern /users/:id path /users/123 -> {id=123}
    // pattern /users/:id path /books -> null (no match)
    //
    // private, because only the Router ever needs it. Returning null
    // for "no match" keeps the caller's check to one line; returning
    // a map (never a mutated argument) keeps it easy to reason about.
    private static Map<String, String> match(String pattern, String path) {
        String[] p = pattern.split("/");
        String[] q = path.split("/");

        // Different number of segments can never match.
        if (p.length != q.length) {
            return null;
        }

        Map<String, String> params = new LinkedHashMap<>();
        for (int i = 0; i < p.length; i++) {
            if (p[i].startsWith(":")) {
                // Dynamic segment: bind whatever the client sent.
                params.put(p[i].substring(1), q[i]);
            } else if (!p[i].equals(q[i])) {
                // Static segment: must match exactly.
                return null;
            }
        }
        return params;
    }
}

// ==================== INHERITANCE ====================

// BaseHandler holds behaviour every handler shares. Subclasses extend
// it and get log() for free rather than each writing their own.
//
// abstract: you cannot create a bare BaseHandler. It exists only to be
// extended, and it deliberately does not implement handle() -- that is
// the part each subclass must supply.
abstract class BaseHandler implements Handler {

    private final String name;

    BaseHandler(String name) {
        this.name = name;
    }

    // protected: subclasses may call it, outside code may not.
    protected void log(Request req) {
        System.out.println("[" + name + "] " + req.method + " " + req.path);
    }
}

// GetUser IS-A BaseHandler, which IS-A Handler. Both are true at once,
// and that is what lets the Router hold it in a Map<String, Handler>.
class GetUser extends BaseHandler {

    GetUser(String name) {
        super(name);
    }

    @Override
    public Response handle(Request req) {
        log(req); // inherited from BaseHandler
        return new Response(200, "user id = " + req.params.get("id"));
    }
}

class ListBooks extends BaseHandler {

    ListBooks(String name) {
        super(name);
    }

    @Override
    public Response handle(Request req) {
        log(req);
        // A query parameter is optional by nature, so it always needs
        // a default. A missing ?page= means page 1, not an error.
        String page = req.query.getOrDefault("page", "1");
        return new Response(200, "books page " + page);
    }
}

// ==================== PUTTING IT TOGETHER ====================

public class RouterDemo {
    public static void main(String[] args) {
        Router router = new Router();

        // Two routes, two handlers. The Router stores them as Handler
        // and forgets what they really are.
        router.register("GET", "/api/users/:id", new GetUser("users"));
        router.register("GET", "/api/books", new ListBooks("books"));

        // A dynamic segment: :id is bound to "123".
        System.out.println(
                router.dispatch("GET", "/api/users/123", Map.of()).body);

        // A query parameter: ?page=2 drives pagination.
        System.out.println(
                router.dispatch("GET", "/api/books",
                        Map.of("page", "2")).body);

        // Nothing registered for this path -> the catch-all.
        System.out.println(
                router.dispatch("GET", "/nope", Map.of()).body);
    }
}
