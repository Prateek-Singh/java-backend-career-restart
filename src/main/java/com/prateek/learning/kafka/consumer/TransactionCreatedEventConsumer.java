package com.prateek.learning.kafka.consumer;

import com.prateek.learning.kafka.consumer.handler.TransactionCreatedEventHandler;
import com.prateek.learning.kafka.event.TransactionCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionCreatedEventConsumer {

    private final TransactionCreatedEventHandler handler;

    public TransactionCreatedEventConsumer(TransactionCreatedEventHandler handler) {
        this.handler = handler;
    }

    @KafkaListener(
            topics = "transaction-events",
            groupId = "transaction-created-events-cg"
    )
    public void consume(TransactionCreatedEvent event) {
        handler.handle(event);
    }
}