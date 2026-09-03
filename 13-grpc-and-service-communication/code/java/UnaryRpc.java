// System Design - Backend
// Chapter 13, gRPC & Service Communication -> 06 Unary RPC
// Java 21 / grpc-java 1.62

package com.example.grpc.unary;

// ===== SERVER =====
// protoc generates UserServiceGrpc.UserServiceImplBase. Every method
// takes the request plus a StreamObserver to answer on -- there is no
// return value, which is exactly what lets the same shape carry
// streaming later.
@GrpcService
class UserService extends UserServiceGrpc.UserServiceImplBase {
    
    @Override
    public void getUser(GetUserRequest request,
                        StreamObserver<GetUserResponse> responseObserver) {
        
        if (request.getId().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                                         .withDescription("id is required")
                                         .asRuntimeException()); // typed error, sec 14
            return;
        }
        
        User user = User.newBuilder()
                        .setId(request.getId())
                        .setEmail("ada@example.com")
                        .setFullName("Ada")
                        .setRole(Role.ROLE_ADMIN)
                        .build();
        
        responseObserver.onNext(
            GetUserResponse.newBuilder().setUser(user).build());
        responseObserver.onCompleted(); // ALWAYS: without it the call hangs
    }
}

class UnaryRpcClient {
    
    // ===== CLIENT =====
    void callGetUser() {
        // usePlaintext is for local development only.
        ManagedChannel channel = ManagedChannelBuilder
                                     .forAddress("localhost", 50051)
                                     .usePlaintext()
                                     .build();
        
        var stub = UserServiceGrpc.newBlockingStub(channel)
                       .withDeadlineAfter(1, TimeUnit.SECONDS); // always a deadline
        
        try {
            GetUserResponse resp = stub.getUser(
                GetUserRequest.newBuilder().setId("u42").build());
            log.info("got user: {}", resp.getUser().getFullName());
        } catch (StatusRuntimeException e) {
            log.error("GetUser failed: {}", e.getStatus()); // carries the code
        } finally {
            channel.shutdown();
        }
    }
}
