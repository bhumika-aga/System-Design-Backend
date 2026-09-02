// System Design - Backend
// Chapter 22, Automated Testing -> 16 Performance & load testing
// Java 21 / JUnit 5 / AssertJ / Mockito / Testcontainers

package com.example.testing;

// JMH handles warm-up, JIT compilation and dead-code elimination. On the
// JVM a naive System.nanoTime() loop measures the optimiser, not you.
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ParseEventBenchmark {

    private final byte[] payload = """
            {"type":"click","ts":1717000000}
            """.getBytes(UTF_8);

    @Benchmark
    public Event parseEvent() {
        return EventParser.parse(payload); // returned, so JIT cannot elide
    }
}

// Run: mvn clean verify && java -jar target/benchmarks.jar
// ParseEventBenchmark.parseEvent avgt 382.4 ns/op
// Store the numbers and compare across runs to catch REGRESSIONS.
