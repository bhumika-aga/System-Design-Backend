// System Design - Backend
// Chapter 20, Concurrency & Parallelism -> 09 Virtual Threads
// Java 21 / Spring Boot 3.3

package com.example.concurrency.vthreads;

// On Java 21, Spring Boot serves each request on its own virtual
// thread if you set one property. Tomcat stops handing work to a
// fixed pool of ~200 platform threads and starts a fresh virtual
// thread per request instead:
//
//   spring.threads.virtual.enabled: true
//
// Underneath, that is this loop:
final class VirtualThreadServer {
    
    public static void main(String[] args) throws Exception {
        Executor executor = Executors.newVirtualThreadPerTaskExecutor();
        
        try (ServerSocket listener = new ServerSocket(8080)) {
            while (true) {
                Socket conn = listener.accept(); // wait for a connection
                
                // A NEW virtual thread for every connection. A million of
                // these is ordinary; a million OS threads is impossible.
                executor.execute(() -> handle(conn));
            }
        }
    }
}
