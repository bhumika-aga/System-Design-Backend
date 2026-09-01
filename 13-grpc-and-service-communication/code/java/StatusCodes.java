// System Design - Backend
// Chapter 13, gRPC & Service Communication -> 14 Status codes & error handling
// Java 21 / grpc-java 1.62

package com.example.grpc.status;

@GrpcService
class StatusCodesServer extends UserServiceGrpc.UserServiceImplBase {

    // SERVER: answer with a typed status, never a bare exception message.
    @Override
    public void getUser(GetUserRequest request,
            StreamObserver<GetUserResponse> observer) {

        if (request.getId().isEmpty()) {
            observer.onError(Status.INVALID_ARGUMENT
                    .withDescription("id is required")
                    .asRuntimeException());
            return;
        }

        Optional<User> user = db.find(request.getId());
        if (user.isEmpty()) {
            observer.onError(Status.NOT_FOUND
                    .withDescription("no user with id " + request.getId())
                    .asRuntimeException());
            return;
        }

        observer.onNext(GetUserResponse.newBuilder()
                .setUser(user.get())
                .build());
        observer.onCompleted();
    }
}

class StatusCodesClient {

    void demo() throws Exception {
        // CLIENT: switch on the code to decide what to do about it.
        try {
            GetUserResponse resp = stub.getUser(request);
        } catch (StatusRuntimeException e) {
            switch (e.getStatus().getCode()) {
                case NOT_FOUND -> showNotFound(); // expected
                case UNAVAILABLE -> retryWithBackoff(); // transient, sec 17
                default -> log.error("unexpected {}: {}",
                        e.getStatus().getCode(),
                        e.getStatus().getDescription());
            }
        }
        // Letting an ordinary exception escape a handler hands the client
        // UNKNOWN with no description at all: the gRPC equivalent of a bare
        // 500. Map it deliberately, the way chapter 12 maps to HTTP codes.
    }
}
