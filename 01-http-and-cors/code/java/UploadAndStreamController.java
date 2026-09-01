// System Design - Backend
// Chapter 01, HTTP & CORS -> Large downloads and streaming
// Java 21 / Spring Boot 3.3

package com.example.http;

import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
class UploadAndStreamController {

    // 1. Receive a multipart upload
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String upload(@RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no file");
        }
        return "received " + file.getOriginalFilename();
    }
    // Limits live in application.yml, enforced before your method runs:
    // spring.servlet.multipart.max-file-size: 32MB
    // spring.servlet.multipart.max-request-size: 32MB

    // 2. Stream a response in chunks (Server-Sent Events)
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(1).toMillis());

        Thread.ofVirtual().start(() -> { // Java 21: blocking here is cheap
            try {
                for (int i = 0; i < 5; i++) {
                    // each send() is flushed to the client immediately
                    emitter.send(SseEmitter.event().data("chunk " + i));
                    Thread.sleep(Duration.ofSeconds(1));
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }
}
