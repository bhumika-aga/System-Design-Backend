// System Design - Backend
// Chapter 10, Task Queues & Background Jobs -> 11 Consumer
// Java 21 / Spring Boot 3.3

package com.example.tasks.queue;

@Component
class EmailTaskConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(EmailTaskConsumer.class);
    
    private final EmailService emails;
    
    EmailTaskConsumer(EmailService emails) {
        this.emails = emails;
    }
    
    // Spring deserialises the JSON into the record, so step 1 of the
    // hand-written version disappears entirely.
    @RabbitListener(queues = "email.send_verification")
    void handle(EmailPayload payload) {
        
        log.info("sending verification email userId={} email={}",
            payload.userId(), payload.email());
        
        emails.sendVerification(payload.email(), payload.token());
        
        // Returning normally ACKs the message. Throwing sends it back
        // for retry, and after the configured attempts it lands in
        // the dead-letter queue.
    }
    
    // A message that can NEVER succeed should not be retried at all:
    // AmqpRejectAndDontRequeueException means "this one is hopeless",
    // and is the equivalent of Asynq's SkipRetry.
    
    @RabbitListener(queues = "email.send_verification.dlq")
    void handleDead(EmailPayload payload) {
        log.error("giving up on verification email for {}",
            payload.userId());
    }
}
