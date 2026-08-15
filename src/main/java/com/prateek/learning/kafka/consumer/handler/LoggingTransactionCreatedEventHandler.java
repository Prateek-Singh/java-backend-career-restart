package com.prateek.learning.kafka.consumer.handler;

import com.prateek.learning.kafka.event.TransactionCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingTransactionCreatedEventHandler
        implements TransactionCreatedEventHandler {

    private static final Logger log =
            LoggerFactory.getLogger(LoggingTransactionCreatedEventHandler.class);

    @Override
    public void handle(TransactionCreatedEvent event) {
        log.info(
                "Handling TransactionCreatedEvent with id: {}, transactionId: {}, accountId: {}",
                event.eventId(),
                event.transactionId(),
                event.accountId()
        );
    }
}
