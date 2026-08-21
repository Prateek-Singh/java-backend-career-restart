package com.prateek.learning.kafka.consumer;

import com.prateek.learning.kafka.consumer.handler.TransactionCreatedEventHandler;
import com.prateek.learning.kafka.event.TransactionCreatedEvent;
import jakarta.validation.Valid;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionCreatedEventConsumer {

    private final TransactionCreatedEventHandler handler;

    public TransactionCreatedEventConsumer(TransactionCreatedEventHandler handler) {
        this.handler = handler;
    }

    @KafkaListener(
            topics = "${app.kafka.transaction-events.topic:transaction-events}",
            groupId = "${app.kafka.transaction-events.group-id:transaction-created-events-cg}"
    )
    public void consume(@Valid TransactionCreatedEvent event) {
        handler.handle(event);
    }
}