// System Design - Backend
// Chapter 13, gRPC & Service Communication -> 08 Client streaming
// Java 21 / grpc-java 1.62

package com.example.grpc.streaming;

@GrpcService
class ClientStreamingServer extends UserServiceGrpc.UserServiceImplBase {
    
    // proto: rpc UploadEvents(stream Event) returns (UploadSummary);
    
    // ===== SERVER: return the observer the client writes into =====
    @Override
    public StreamObserver<Event> uploadEvents(
        StreamObserver<UploadSummary> responseObserver) {
        
        return new StreamObserver<>() {
            private int count = 0;
            
            @Override
            public void onNext(Event event) {
                store(event);
                count++;
            }
            
            @Override
            public void onError(Throwable t) {
                log.warn("upload aborted after {} events", count, t);
            }
            
            @Override
            public void onCompleted() { // client finished sending
                responseObserver.onNext(UploadSummary.newBuilder()
                                            .setReceived(count)
                                            .build());
                responseObserver.onCompleted(); // the single reply
            }
        };
    }
}

class ClientStreamingClient {
    
    // ===== CLIENT: needs the ASYNC stub; a blocking one cannot send =====
    void uploadEvents(UserServiceGrpc.UserServiceStub stub,
                      List<Event> events) throws InterruptedException {
        
        CountDownLatch done = new CountDownLatch(1);
        
        StreamObserver<Event> requests = stub.uploadEvents(
            new StreamObserver<UploadSummary>() {
                @Override
                public void onNext(UploadSummary s) {
                    log.info("server received {} events", s.getReceived());
                }
                
                @Override
                public void onError(Throwable t) {
                    done.countDown();
                }
                
                @Override
                public void onCompleted() {
                    done.countDown();
                }
            });
        
        events.forEach(requests::onNext);
        requests.onCompleted(); // close our side
        done.await(30, TimeUnit.SECONDS); // and wait for the summary
    }
}
