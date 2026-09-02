// System Design - Backend
// Chapter 23, Message Brokers & Kafka -> 07 Consumers & consumer groups
// Java 21 / Spring Boot 3.3 / Spring for Apache Kafka

package com.example.kafka;

// The group id lives in application.yaml. Scale by running more copies of
// the app: Kafka rebalances the partitions across the group for you.
//
//   spring.kafka.consumer:
//     group-id: billing
//     auto-offset-reset: earliest  # no committed offset yet (sec 8)
//     enable-auto-commit: false    # commit by hand for at-least-once
//   spring.kafka.listener:
//     ack-mode: manual             # the listener decides when to commit
@Component
class BillingListener {

    @KafkaListener(topics = "orders")
    void onOrder(ConsumerRecord<String, String> record,
            Acknowledgment ack) {

        process(record.value()); // do the work FIRST...
        ack.acknowledge(); // ...THEN commit the offset

        // Crash between those two lines and the record is redelivered.
        // That is at-least-once, which is why process() has to be
        // idempotent (sec 9).
    }
}
