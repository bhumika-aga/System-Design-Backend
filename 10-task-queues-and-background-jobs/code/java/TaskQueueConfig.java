// System Design - Backend
// Chapter 10, Task Queues & Background Jobs -> 11 Queues, DLQ and concurrency
// Java 21 / Spring Boot 3.3

package com.example.tasks.queue;

@Configuration
class TaskQueueConfig {

    // The work queue, with its dead-letter queue named up front.
    @Bean
    Queue emailQueue() {
        return QueueBuilder.durable("email.send_verification")
                .deadLetterExchange("tasks.dlx")
                .deadLetterRoutingKey("email.send_verification.dlq")
                .build();
    }

    @Bean
    Queue emailDlq() {
        return QueueBuilder.durable("email.send_verification.dlq").build();
    }

    // JSON on the wire, so a payload stays readable in the broker UI.
    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Concurrency and prefetch: the Asynq Config equivalent.
    @Bean
    SimpleRabbitListenerContainerFactory listenerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter converter) {

        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setConcurrentConsumers(10); // 10 workers
        factory.setMaxConcurrentConsumers(20); // burst to 20
        factory.setPrefetchCount(1); // one in flight each
        return factory;
    }
}
// Asynq's weighted queues ("email: 6, default: 3, low: 1") become
// separate queues here, with more consumers on the ones that matter.
// Retry lives in application.yml:
// spring.rabbitmq.listener.simple.retry:
// enabled: true
// max-attempts: 5
// initial-interval: 2s
// multiplier: 2
