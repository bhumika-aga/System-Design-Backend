// System Design - Backend
// Chapter 13, gRPC & Service Communication -> 10 The .proto to code workflow
// Java 21 / grpc-java 1.62

package com.example.grpc.build;

// The build runs protoc for you. Nothing to install by hand, and no
// binary to keep current on your PATH.
//
// pom.xml:
// <plugin>
// <groupId>org.xolstice.maven.plugins</groupId>
// <artifactId>protobuf-maven-plugin</artifactId>
// <configuration>
// <protocArtifact>
// com.google.protobuf:protoc:3.25.3:exe:${os.detected.classifier}
// </protocArtifact>
// <pluginId>grpc-java</pluginId>
// <pluginArtifact>
// io.grpc:protoc-gen-grpc-java:1.62.2:exe:${os.detected.classifier}
// </pluginArtifact>
// </configuration>
// </plugin>
//
// Put user.proto in src/main/proto/ and build:
// ./mvnw compile
//
// Generates into target/generated-sources/protobuf/:
// User.java, GetUserRequest.java, ... one class per message,
// immutable, with builders
// UserServiceGrpc.java the stubs + the base class
//
// Those files are BUILD OUTPUT: never committed, regenerated every
// build. That is precisely what stops the contract and the code from
// quietly drifting apart.
