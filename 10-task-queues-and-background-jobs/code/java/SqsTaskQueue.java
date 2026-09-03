// System Design - Backend
// Chapter 10, Task Queues & Background Jobs -> 06 SQS producer + consumer
// Java 21 / Spring Boot 3.3

package com.example.tasks.sqs;

@Component
class SqsTaskQueue {
    
    // AWS SDK v2 for Java: the same SDK and the same concepts.
    
    // -- PRODUCER ------------------------------------------
    void enqueueEmailTask(SqsClient sqs, String queueUrl, EmailPayload payload)
        throws JsonProcessingException {
        
        sqs.sendMessage(SendMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .messageBody(mapper.writeValueAsString(payload))
                            // For a FIFO queue add:
                            // .messageGroupId("email-group")
                            // .messageDeduplicationId(payload.userId())
                            .build());
    }
    
    // -- CONSUMER ------------------------------------------
    void pollQueue(SqsClient sqs, String queueUrl) {
        while (true) {
            ReceiveMessageResponse result = sqs.receiveMessage(
                ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10) // batch up to 10
                    .waitTimeSeconds(20) // long-poll
                    .visibilityTimeout(60) // 60s before redelivery
                    .build());
            
            for (Message msg : result.messages()) {
                try {
                    process(msg);
                    // ACK: delete only once the work has succeeded
                    sqs.deleteMessage(DeleteMessageRequest.builder()
                                          .queueUrl(queueUrl)
                                          .receiptHandle(msg.receiptHandle())
                                          .build());
                } catch (Exception e) {
                    // Do nothing. The visibility timeout lapses and SQS
                    // hands the message to another consumer.
                }
            }
        }
    }
    // Spring Cloud AWS wraps all of this in @SqsListener, which owns the
    // receive loop, the ack and the error path for you.
}
