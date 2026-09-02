// System Design - Backend
// Chapter 24, WebSockets & Real-Time -> 07 Connection management
// Java 21 / Spring Boot 3.3 / Spring WebSocket

package com.example.websockets;

// Spring shares ONE handler instance across every session, so the registry
// has to be thread-safe. A ConcurrentHashMap does the job the Go hub used
// an owning goroutine for.
@Component
class Hub extends TextWebSocketHandler {

    private final Map<String, Client> clients = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        clients.put(session.getId(), new Client(session));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,
            CloseStatus status) {
        Client gone = clients.remove(session.getId());
        if (gone != null) {
            gone.stop(); // its writer thread finishes
        }
    }

    void broadcast(String message) {
        // offer() is false when a client's buffer is full. That is a slow
        // consumer, so drop it rather than stall everyone else (sec 12).
        clients.values().removeIf(client -> !client.offer(message));
    }
}

// One bounded queue and one virtual thread per client, so a slow reader
// blocks only itself.
final class Client {

    private final BlockingQueue<String> out = new ArrayBlockingQueue<>(64);
    private final WebSocketSession session;
    private volatile boolean running = true;

    Client(WebSocketSession session) {
        this.session = session;
        Thread.ofVirtual().start(this::drain);
    }

    boolean offer(String message) {
        return out.offer(message); // never blocks the broadcaster
    }

    void stop() {
        running = false;
    }

    private void drain() {
        try {
            while (running) {
                session.sendMessage(new TextMessage(out.take()));
            }
        } catch (Exception e) {
            running = false; // socket gone; let the thread end
        }
    }
}
