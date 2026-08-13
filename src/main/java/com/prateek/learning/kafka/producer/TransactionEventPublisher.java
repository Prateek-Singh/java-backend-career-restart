package com.prateek.learning.kafka.producer;

import com.prateek.learning.kafka.event.TransactionCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventPublisher {

    private static final String TOPIC = "transaction-events";

    private final KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

    public TransactionEventPublisher(
            KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(TransactionCreatedEvent event) {
        kafkaTemplate.send(
                TOPIC,
                event.transactionId(),
                event
        );
    }
}
