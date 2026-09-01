// System Design - Backend
// Chapter 13, gRPC & Service Communication -> 16 Authentication & security
// Java 21 / grpc-java 1.62

package com.example.grpc.security;

class TransportSecurityServerDemo {

    void demo() throws Exception {
        // ===== SERVER over TLS =====
        Server server = NettyServerBuilder.forPort(443)
                .useTransportSecurity(new File("server.crt"),
                        new File("server.key"))
                .addService(new UserService())
                .build();

        // ===== CLIENT over TLS =====
        ManagedChannel channel = NettyChannelBuilder
                .forAddress("api.example.com", 443)
                .sslContext(GrpcSslContexts.forClient()
                        .trustManager(new File("ca.crt")) // trust this CA
                        .build())
                .build();
    }
}

// ===== Per-call token, on top of TLS =====
// CallCredentials attaches a FRESH token to every call, so a rotated
// or refreshed token is picked up without rebuilding the channel.
class BearerToken extends CallCredentials {

    private static final Metadata.Key<String> AUTHORIZATION = Metadata.Key.of("authorization",
            Metadata.ASCII_STRING_MARSHALLER);

    private final Supplier<String> token;

    BearerToken(Supplier<String> token) {
        this.token = token;
    }

    @Override
    public void applyRequestMetadata(RequestInfo info, Executor executor,
            MetadataApplier applier) {
        executor.execute(() -> {
            Metadata headers = new Metadata();
            headers.put(AUTHORIZATION, "Bearer " + token.get());
            applier.apply(headers);
        });
    }
}

class TransportSecurityClientDemo {

    void demo() throws Exception {

        var stub = UserServiceGrpc.newBlockingStub(channel)
                .withCallCredentials(new BearerToken(tokens::current));

        // gRPC refuses to send CallCredentials over a plaintext channel, so
        // the token cannot leak through misconfiguration -- the same promise
        // Go makes with RequireTransportSecurity().
    }
}
