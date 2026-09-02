// System Design - Backend
// Chapter 24, WebSockets & Real-Time -> 06 A WebSocket client
// Java 21 / Spring Boot 3.3 / Spring WebSocket

package com.example.websockets;

class EchoClient {

    private static final Logger log = LoggerFactory.getLogger(EchoClient.class);

    void run(String token) throws Exception {
        // Handshake headers carry the token where the platform allows
        // them, which a browser does not but a server-side client does.
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.setBearerAuth(token); // (sec 13)

        WebSocketClient client = new StandardWebSocketClient();

        // No reader thread to start: Spring hands each frame to the
        // handler as it arrives.
        WebSocketSession session = client.execute(
                new TextWebSocketHandler() {
                    @Override
                    protected void handleTextMessage(WebSocketSession s,
                            TextMessage m) {
                        log.info("recv: {}", m.getPayload());
                    }
                },
                headers,
                URI.create("wss://api.example.com/ws")).get();

        session.sendMessage(new TextMessage("{\"type\":\"hello\"}"));

        // Graceful close: send a close frame, then the socket shuts (sec 6)
        session.close(CloseStatus.NORMAL.withReason("bye"));
    }
}
