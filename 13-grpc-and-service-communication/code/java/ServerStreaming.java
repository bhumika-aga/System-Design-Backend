// System Design - Backend
// Chapter 13, gRPC & Service Communication -> 07 Server streaming
// Java 21 / grpc-java 1.62

package com.example.grpc.streaming;

@GrpcService
class ServerStreamingServer extends UserServiceGrpc.UserServiceImplBase {

    // proto: rpc ListUsers(ListUsersRequest) returns (stream User);

    // ===== SERVER: onNext repeatedly, then onCompleted once =====
    @Override
    public void listUsers(ListUsersRequest request,
            StreamObserver<User> responseObserver) {

        for (User u : queryUsers(request.getFilter())) {
            responseObserver.onNext(u); // push one message down
        }
        responseObserver.onCompleted(); // closes the stream cleanly
    }
}

class ServerStreamingClient {

    // ===== CLIENT: the blocking stub hands back an Iterator =====
    void listUsers(UserServiceGrpc.UserServiceBlockingStub stub) {

        Iterator<User> users = stub
                .withDeadlineAfter(10, TimeUnit.SECONDS)
                .listUsers(ListUsersRequest.newBuilder()
                        .setFilter("active")
                        .build());

        while (users.hasNext()) {
            log.info("user: {}", users.next().getFullName());
        }
    }
    // There is no EOF sentinel to compare against here. Exhausting the
    // iterator IS the end-of-stream signal, and a failure mid-stream
    // surfaces as a StatusRuntimeException out of next().
}
