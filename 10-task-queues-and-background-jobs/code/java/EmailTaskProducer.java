// System Design - Backend
// Chapter 10, Task Queues & Background Jobs -> 11 Producer + payload
// Java 21 / Spring Boot 3.3

package com.example.tasks.queue;

// The payload. A record serialises to JSON and back with no work.
record EmailPayload(String userId, String email, String token) {
}

@Service
class EmailTaskProducer {

    static final String EXCHANGE = "tasks";
    static final String ROUTING_KEY = "email.send_verification";

    private final RabbitTemplate rabbit;

    EmailTaskProducer(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    // Returns as soon as the broker accepts the message. The caller,
    // an HTTP handler, never waits for the email to be sent.
    void sendVerificationEmail(String userId, String email, String token) {
        rabbit.convertAndSend(EXCHANGE, ROUTING_KEY,
                new EmailPayload(userId, email, token));
    }
}
// Unlike Asynq, the retry count is not attached to the message. It
// belongs to the queue, declared once beside its dead-letter queue.
