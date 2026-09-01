// System Design - Backend
// Chapter 10, Task Queues & Background Jobs -> 21 Temporal workflow
// Java 21 / Spring Boot 3.3

package com.example.tasks.workflow;

// Temporal's Java SDK. The interface declares the entry point; the
// implementation is ordinary Java. Temporal persists every completed
// step, so a crash resumes exactly where it stopped.
@WorkflowInterface
interface VideoProcessingWorkflow {

        @WorkflowMethod
        void process(String videoId);
}

class VideoProcessingWorkflowImpl implements VideoProcessingWorkflow {

        // Every step gets its own timeout and retry policy.
        private static final ActivityOptions OPTIONS = ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofMinutes(10))
                        .setRetryOptions(RetryOptions.newBuilder()
                                        .setMaximumAttempts(3)
                                        .setInitialInterval(Duration.ofMinutes(1))
                                        .setBackoffCoefficient(2.0)
                                        .build())
                        .build();

        private final VideoActivities activities = Workflow.newActivityStub(VideoActivities.class, OPTIONS);

        @Override
        public void process(String videoId) {

                // Step 1. A crash here resumes from step 1 on restart.
                String encodedPath = activities.encode(videoId);

                // Steps 2 and 3 run in parallel.
                Promise<Void> thumbnails = Async.procedure(activities::generateThumbnails, encodedPath);
                Promise<Void> transcript = Async.procedure(activities::generateTranscription, encodedPath);

                thumbnails.get(); // each has already retried on its own
                transcript.get();

                // Step 4 depends on step 2 having finished.
                activities.processThumbnailImages(videoId);

                // Step 5.
                activities.notifyUserVideoReady(videoId);
        }
}
