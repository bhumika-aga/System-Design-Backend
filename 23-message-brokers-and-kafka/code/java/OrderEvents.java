// System Design - Backend
// Chapter 23, Message Brokers & Kafka -> 06 Producers
// Java 21 / Spring Boot 3.3 / Spring for Apache Kafka

package com.example.kafka;

// Spring for Apache Kafka wraps the official Java client. Almost all of
// the producer tuning lives in application.yaml rather than in code:
//
//   spring.kafka.producer:
//     acks: all                   # leader + in-sync replicas (sec 5)
//     properties:
//       enable.idempotence: true  # no duplicates on retry (sec 9)
//       linger.ms: 5              # wait up to 5ms to batch (throughput)
//       compression.type: zstd
@Service
class OrderEvents {
    
    private static final Logger log = LoggerFactory.getLogger(OrderEvents.class);
    
    private final KafkaTemplate<String, String> kafka;
    
    OrderEvents(KafkaTemplate<String, String> kafka) {
        this.kafka = kafka;
    }
    
    void placed(String orderId, String payload) {
        // The KEY picks the partition, so every event for one order lands
        // on the same partition and stays in order (sec 4, sec 10).
        CompletableFuture<SendResult<String, String>> sent = kafka.send(
            "orders",
            orderId,
            payload);
        
        // Sending is asynchronous and batched. The future carries the
        // final partition and offset, or the broker's error.
        sent.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("publish failed for {}", orderId, ex);
                return;
            }
            RecordMetadata md = result.getRecordMetadata();
            log.info("published to {}-{} at offset {}",
                md.topic(), md.partition(), md.offset());
        });
    }
}
