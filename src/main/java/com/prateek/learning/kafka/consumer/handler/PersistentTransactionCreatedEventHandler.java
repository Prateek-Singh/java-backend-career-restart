package com.prateek.learning.kafka.consumer.handler;

import com.prateek.learning.kafka.event.TransactionCreatedEvent;
import com.prateek.learning.kafka.processing.TransactionEventProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PersistentTransactionCreatedEventHandler
        implements TransactionCreatedEventHandler {

    private static final Logger log =
            LoggerFactory.getLogger(PersistentTransactionCreatedEventHandler.class);

    private final TransactionEventProcessingService processingService;

    public PersistentTransactionCreatedEventHandler(
            TransactionEventProcessingService processingService) {
        this.processingService = processingService;
    }

    @Override
    public void handle(TransactionCreatedEvent event) {
        log.info(
                "Processing TransactionCreatedEvent eventId={}, transactionId={}",
                event.eventId(),
                event.transactionId()
        );

        processingService.process(event);
    }
}
