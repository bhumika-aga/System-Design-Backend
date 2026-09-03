// System Design - Backend
// Chapter 20, Concurrency & Parallelism -> 12 Locks, Mutexes & Queues
// Java 21 / Spring Boot 3.3

package com.example.concurrency.locks;

final class Counter {
    
    public static void main(String[] args) throws Exception {
        int[] counter = {0};
        ReentrantLock lock = new ReentrantLock();
        
        // close() waits for every submitted task to finish, so the
        // try-with-resources block is the join.
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 1000; i++) {
                pool.execute(() -> {
                    lock.lock(); // others queue up here
                    try {
                        counter[0]++; // now one step, not three
                    } finally {
                        lock.unlock(); // always release in finally
                    }
                });
            }
        }
        
        System.out.println(counter[0]); // always 1000
    }
}

// For a counter specifically, do not reach for a lock at all. An
// atomic does the same read-modify-write as one CPU instruction,
// with no thread ever waiting:
final class Counters {
    
    void demo() {
        AtomicInteger hits = new AtomicInteger();
        hits.incrementAndGet();
    }
}
