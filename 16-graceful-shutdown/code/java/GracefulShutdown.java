// System Design - Backend
// Chapter 16, Graceful Shutdown -> 7.1 Graceful shutdown in Java
// Java 21 / Spring Boot 3.3

package com.example.shutdown;

// Spring Boot has graceful shutdown built in. Two lines of config
// replace the signal handler, the background thread and the drain
// loop entirely:
//
//   server:
//     shutdown: graceful      # stop accepting, finish what is in flight
//   spring:
//     lifecycle:
//       timeout-per-shutdown-phase: 30s     # the hard limit
//
// SIGTERM (Kubernetes, systemd) and SIGINT (Ctrl+C) both reach the
// JVM's shutdown hook, and Spring treats them identically -- the
// intention is the same either way.

@SpringBootApplication
class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// Cleanup is per-bean, and the ORDER IS AUTOMATIC: Spring destroys
// beans in reverse order of creation, so anything built from the
// DataSource is torn down before the DataSource itself.
@Component
class BackgroundJobServer implements SmartLifecycle {
    
    private static final Logger log = LoggerFactory.getLogger(BackgroundJobServer.class);
    
    private volatile boolean running;
    
    @Override
    public void start() {
        running = true;
        log.info("job server started");
    }
    
    @Override
    public void stop() {
        log.info("stopping background job server...");
        drainWorkers(); // let workers finish the job in hand
        running = false;
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    // Phase orders the lifecycle beans among themselves. The web
    // server sits at Integer.MAX_VALUE and stops FIRST, so a lower
    // phase stops after it -- which is what draining HTTP before
    // the workers requires.
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 1;
    }
}

// When there is nothing to coordinate, @PreDestroy is enough.
@Component
class ReportBuffer {
    
    @PreDestroy
    void flush() {
        log.info("flushing pending reports before exit");
    }
}

// The DataSource needs no code at all: Spring closes the pool as the
// context shuts down, after every bean that depended on it is gone.
//
// One thing configuration cannot do for you. Kubernetes sends
// SIGTERM and removes the pod from the Service at the same moment,
// so a request already in flight can still arrive AFTER draining has
// begun. A preStop hook that sleeps a few seconds gives the endpoint
// removal time to propagate first:
//
// lifecycle:
// preStop:
// exec:
// command: ["sh", "-c", "sleep 5"]
