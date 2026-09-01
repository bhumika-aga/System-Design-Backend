// System Design - Backend
// Chapter 13, gRPC & Service Communication -> 09 Bidirectional streaming
// Java 21 / grpc-java 1.62

package com.example.grpc.streaming;

@GrpcService
class BidirectionalStreamingServer extends UserServiceGrpc.UserServiceImplBase {

    // proto: rpc Chat(stream ChatMessage) returns (stream ChatMessage);

    // ===== SERVER: read and write on the same call, in any order =====
    @Override
    public StreamObserver<ChatMessage> chat(
            StreamObserver<ChatMessage> responseObserver) {

        return new StreamObserver<>() {

            @Override
            public void onNext(ChatMessage msg) {
                // Reply now, or broadcast to a room. Nothing requires one
                // response per request: that is the whole point of bidi.
                responseObserver.onNext(ChatMessage.newBuilder()
                        .setUser("server")
                        .setText("ack: " + msg.getText())
                        .build());
            }

            @Override
            public void onError(Throwable t) {
                log.warn("chat stream failed", t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted(); // client closed its side
            }
        };
    }
}

class BidirectionalStreamingClient {

    // ===== CLIENT =====
    void chat(UserServiceGrpc.UserServiceStub stub) {

        StreamObserver<ChatMessage> requests = stub.chat(
                new StreamObserver<ChatMessage>() {
                    @Override
                    public void onNext(ChatMessage in) {
                        log.info("<< {}", in.getText());
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onCompleted() {
                    }
                });

        // Receiving needs no thread of your own: the callback already runs
        // on the channel's executor, so sending is an ordinary loop.
        for (String text : List.of("hi", "how are you", "bye")) {
            requests.onNext(ChatMessage.newBuilder()
                    .setUser("ada")
                    .setText(text)
                    .build());
        }
        requests.onCompleted(); // done sending; replies keep arriving
    }
}
