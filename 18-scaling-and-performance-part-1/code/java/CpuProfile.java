// System Design - Backend
// Chapter 18, Scaling & Performance -> 07 Profiling & flame graphs
// Java 21 / Spring Boot 3.3

package com.example.scaling.profiling;

// Java Flight Recorder ships inside the JDK. Nothing to add.
//
// Record for 30 seconds at startup:
//   java -XX:StartFlightRecording=duration=30s,filename=cpu.jfr \
//        -jar app.jar
//
// Or attach to a process that is already misbehaving:
//   jcmd <pid> JFR.start duration=30s filename=cpu.jfr
//
// Open cpu.jfr in JDK Mission Control for the flame graph.
// You can also drive a recording from code, which is useful in a
// benchmark or a test that reproduces the slow path:
final class CpuProfile {

    static void record(Path out, Duration window) throws Exception {
        Configuration profile = Configuration.getConfiguration("profile");
        try (Recording recording = new Recording(profile)) {
            recording.setDestination(out);
            recording.start();
            Thread.sleep(window.toMillis());
            recording.stop(); // writes the file on stop
        }
    }
}
