// System Design - Backend
// Chapter 20, Concurrency & Parallelism -> 12 Locks, Mutexes & Queues
// Java 21 / Spring Boot 3.3

package com.example.concurrency.locks;

// Java has no channels, but the idea carries over: give ONE thread
// sole ownership of the state, and let everyone else send it
// messages. A BlockingQueue is the pipe between them.
final class CounterService {

    public static void main(String[] args) throws Exception {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(100);
        CompletableFuture<Integer> total = new CompletableFuture<>();

        Thread owner = Thread.ofVirtual().start(() -> {
            int counter = 0; // ONLY this thread touches it
            try {
                for (int i = 0; i < 1000; i++) {
                    counter += queue.take(); // waits for a value
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            total.complete(counter);
        });

        // 1000 senders. None of them can see `counter`, so none of
        // them can corrupt it.
        for (int i = 0; i < 1000; i++) {
            queue.put(1);
        }

        System.out.println(total.get()); // 1000, and no lock anywhere
        owner.join();
    }
}
