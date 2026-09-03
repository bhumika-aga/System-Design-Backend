// System Design - Backend
// Chapter 10, Task Queues & Background Jobs -> 17 Exactly-once via SQS FIFO
// Java 21 / Spring Boot 3.3

package com.example.tasks.sqs;

@Component
class SqsFifoDedup {
    
    private final SqsClient sqs;
    
    SqsFifoDedup(SqsClient sqs) {
        this.sqs = sqs;
    }
    
    void enqueueOrder(String orderId, String payload) {
        sqs.sendMessage(SendMessageRequest.builder()
                            .queueUrl("https://sqs.us-east-1.amazonaws.com/123/orders.fifo")
                            .messageBody(payload)
                            .messageGroupId("order-processing") // ordering group
                            .messageDeduplicationId(orderId) // per business event
                            .build());
    }
}
// Send the same orderId twice within five minutes and SQS silently
// drops the second.
//
// The deduplication id must be the BUSINESS key. A fresh UUID per
// attempt is unique every time, so it deduplicates nothing at all.
