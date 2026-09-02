// System Design - Backend
// Chapter 24, WebSockets & Real-Time -> 05 A WebSocket server
// Java 21 / Spring Boot 3.3 / Spring WebSocket

package com.example.websockets;

// Spring's WebSocket support. The handler is an ordinary bean; the config
// maps it onto a path and states who is allowed to connect.
@Configuration
@EnableWebSocket
class WebSocketConfig implements WebSocketConfigurer {

    private final EchoHandler handler;

    WebSocketConfig(EchoHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws")
                // Guards against cross-site hijacking. Never "*" (sec 14).
                .setAllowedOrigins("https://app.example.com");
    }
}

// Spring runs the read loop for you and calls these back, so there is no
// `for { ReadMessage() }` to write and no socket to close by hand.
@Component
class EchoHandler extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // The 101 handshake has completed (sec 4); the socket is open.
    }

    @Override
    protected void handleTextMessage(WebSocketSession session,
            TextMessage message)
            throws IOException {
        session.sendMessage(message); // echo it straight back
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,
            CloseStatus status) {
        // Client closed, or the connection died (sec 6). Spring has already
        // released the socket, so only your own cleanup belongs here.
    }
}
