// System Design - Backend
// Chapter 24, WebSockets & Real-Time -> 08 Authentication
// Java 21 / Spring Boot 3.3 / Spring WebSocket

package com.example.websockets;

// AUTHENTICATE DURING THE HANDSHAKE, before the upgrade. A rejected
// handshake is a normal HTTP error, which the client can actually read.
@Component
class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtVerifier jwt;

    AuthHandshakeInterceptor(JwtVerifier jwt) {
        this.jwt = jwt;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler handler,
            Map<String, Object> attributes) {
        String token = UriComponentsBuilder.fromUri(request.getURI())
                .build().getQueryParams().getFirst("token");
        try {
            // Attach the identity. Every later frame on this session can
            // read it back from the session attributes.
            attributes.put("user", jwt.verify(token));
            return true;
        } catch (Exception e) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED); // 401, no upgrade
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler handler, Exception ex) {
    }
}
// Wire it in: registry.addHandler(handler, "/ws")
// .addInterceptors(authHandshakeInterceptor);
