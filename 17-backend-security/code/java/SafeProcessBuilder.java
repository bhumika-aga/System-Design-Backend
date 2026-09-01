// System Design - Backend
// Chapter 17, Backend Security -> 04 Command injection
// Java 21 / Spring Boot 3.3 / Spring Security 6

package com.example.security.injection;

class ImageResizerDemo {

    void demo() throws Exception {
        // VULNERABLE, hands the whole line to a shell interpreter
        new ProcessBuilder("sh", "-c",
                "ffmpeg -i input.jpg -o " + userFilename).start();

        // SAFE, the command and each argument are separate list entries.
        // No shell is involved at all, so ; && $() and friends are just
        // characters in a filename.
        Process p = new ProcessBuilder(
                "ffmpeg",
                "-i", "input.jpg",
                "-vf", "scale=800:600",
                userFilename) // an argument, never shell syntax
                .redirectErrorStream(true)
                .start();

        String output = new String(p.getInputStream().readAllBytes(), UTF_8);

        int exit = p.waitFor();

        // Java has no exec("string") that splits on spaces the way a shell
        // would -- ProcessBuilder always takes a list. The unsafe version
        // above has to go out of its way to invoke sh.
    }
}
