package com.prateek.learning.kafka.consumer.handler;

import com.prateek.learning.kafka.event.TransactionCreatedEvent;

public interface TransactionCreatedEventHandler {
    void handle(TransactionCreatedEvent event);
}
