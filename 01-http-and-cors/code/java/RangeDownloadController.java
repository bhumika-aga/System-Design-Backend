// System Design - Backend
// Chapter 01, HTTP & CORS -> The exchange, step by step
// Java 21 / Spring Boot 3.3

package com.example.http;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;

@RestController
class RangeDownloadController {
    
    // Returning a Resource makes Spring handle Range, 206 Partial Content,
    // 416 and If-Range for you, driven by the file's length and modified time.
    @GetMapping("/download")
    ResponseEntity<Resource> download() throws IOException {
        FileSystemResource file = new FileSystemResource(Path.of("big.zip"));
        
        return ResponseEntity.ok()
                   .contentType(MediaType.APPLICATION_OCTET_STREAM)
                   .lastModified(file.lastModified())
                   .body(file);
    }
}
