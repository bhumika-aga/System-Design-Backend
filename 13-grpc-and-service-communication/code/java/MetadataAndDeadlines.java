// System Design - Backend
// Chapter 13, gRPC & Service Communication -> 13 Metadata, deadlines & cancellation
// Java 21 / grpc-java 1.62

package com.example.grpc.metadata;

class MetadataAndDeadlinesServer {
    
    void demo() throws Exception {
        // ===== CLIENT: a deadline and metadata on the call =====
        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("authorization",
            Metadata.ASCII_STRING_MARSHALLER), "Bearer " + token);
        headers.put(Metadata.Key.of("x-request-id",
            Metadata.ASCII_STRING_MARSHALLER), requestId);
        
        var stub = UserServiceGrpc.newBlockingStub(channel)
                       .withDeadlineAfter(1, TimeUnit.SECONDS) // absolute deadline
                       .withInterceptors(MetadataUtils
                                             .newAttachHeadersInterceptor(headers));
        
        try {
            GetUserResponse resp = stub.getUser(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                log.warn("call timed out"); // a failure a local call never had
            }
        }
    }
}

class MetadataAndDeadlinesClient {
    
    // ===== SERVER: read metadata, respect the inherited deadline =====
    // Metadata is pulled off by a ServerInterceptor and put into the gRPC
    // Context, so a handler reads it from there rather than a parameter.
    @Override
    public void getUser(GetUserRequest request,
                        StreamObserver<GetUserResponse> observer) {
        
        // The client's deadline travelled with the call and already
        // applies to this thread.
        if (Context.current().isCancelled()) {
            observer.onError(Status.CANCELLED.asRuntimeException());
            return;
        }
        
        // Hand Context.current() to slow work so it is abandoned too,
        // rather than finishing a query nobody is waiting for.
        observer.onNext(lookup(request.getId()));
        observer.onCompleted();
    }
}
