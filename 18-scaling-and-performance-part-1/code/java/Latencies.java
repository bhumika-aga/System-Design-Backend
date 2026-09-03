// System Design - Backend
// Chapter 18, Scaling & Performance -> 03 Percentiles: P50, P90, P99
// Java 21 / Spring Boot 3.3

package com.example.scaling.metrics;

// The p-th percentile of a set of latencies. p is 0..100.
// In production you would let Micrometer/Prometheus do this, but
// computing it once by hand is the fastest way to see what it means.
final class Latencies {
    
    static Duration percentile(List<Duration> samples, double p) {
        if (samples.isEmpty()) {
            return Duration.ZERO;
        }
        List<Duration> sorted = new ArrayList<>(samples);
        Collections.sort(sorted);
        
        double rank = (p / 100.0) * (sorted.size() - 1);
        return sorted.get((int) Math.ceil(rank));
    }
    
    public static void main(String[] args) {
        List<Duration> samples = Stream.of(
                12,
                15,
                22,
                48,
                95,
                110,
                340,
                890,
                1200,
                4800)
                                     .map(Duration::ofMillis)
                                     .toList();
        
        System.out.printf("P50: %s%n", percentile(samples, 50));
        System.out.printf("P90: %s%n", percentile(samples, 90));
        System.out.printf("P99: %s%n", percentile(samples, 99));
        // P50: PT0.095S half of all requests were faster than this
        // P90: PT1.2S the slowest tenth start here
        // P99: PT4.8S the number your angriest users actually feel
    }
}
