// System Design - Backend
// Chapter 13, gRPC & Service Communication -> 15 Interceptors
// Java 21 / grpc-java 1.62

package com.example.grpc.interceptor;

// A server interceptor is a filter one layer down: it sees the call
// and its headers before any handler does.
class AuthInterceptor implements ServerInterceptor {
    
    static final Context.Key<AppUser> USER = Context.key("user");
    
    private static final Metadata.Key<String> AUTHORIZATION = Metadata.Key.of(
        "authorization",
        
        Metadata.ASCII_STRING_MARSHALLER);
    
    @Override
    public <Q, S> ServerCall.Listener<Q> interceptCall(
        ServerCall<Q, S> call, Metadata headers,
        ServerCallHandler<Q, S> next) {
        
        String token = headers.get(AUTHORIZATION);
        
        if (token == null || !valid(token)) {
            call.close(Status.UNAUTHENTICATED
                           .withDescription("missing or invalid token"),
                new Metadata());
            // An empty listener: the handler is never reached.
            return new ServerCall.Listener<>() {
            };
        }
        
        // Attach the verified identity for the handler to read.
        Context ctx = Context.current().withValue(USER, parse(token));
        return Contexts.interceptCall(ctx, call, headers, next);
    }
}

class AuthInterceptorClient {
    
    void demo() throws Exception {
        // Register the chain when building the server. The FIRST listed is the
        // outermost, exactly as with Go's ChainUnaryInterceptor.
        Server server = ServerBuilder.forPort(50051)
                            .addService(ServerInterceptors.intercept(new UserService(),
                                new LoggingInterceptor(), new AuthInterceptor()))
                            .build();
    }
}
