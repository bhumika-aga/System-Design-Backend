// System Design - Backend
// Chapter 19, Scaling & Performance II -> 09 Asynchronous processing & queues
// Java 21 / Spring Boot 3.3

package com.example.scaling2.async;

record EmailJob(String to, String subject, String template) {
}

// Producer: called from the request thread. Hand the job over and
// return, so the caller waits for the enqueue, not for the send.
@Service
class EmailQueue {

    private final RabbitTemplate rabbit;

    EmailQueue(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    void enqueue(EmailJob job) {
        rabbit.convertAndSend("emails", job);
    }
}

// Consumer: a separate process, or just a separate thread pool. It
// can be scaled independently of the web tier, which is most of the
// point of moving the work off the request path.
@Component
class EmailWorker {

    @RabbitListener(queues = "emails")
    void handle(EmailJob job) {
        sendEmail(job); // 300ms, and nobody is waiting on it
    }
}
