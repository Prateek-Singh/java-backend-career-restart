package com.prateek.learning.kafka.consumer;

import com.prateek.learning.kafka.consumer.handler.TransactionCreatedEventHandler;
import com.prateek.learning.kafka.event.TransactionCreatedEvent;
import com.prateek.learning.transaction.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionCreatedEventConsumerTest {

    @Mock
    private TransactionCreatedEventHandler handler;

    @InjectMocks
    private TransactionCreatedEventConsumer consumer;

    @Test
    void shouldDelegateConsumedEventToHandler() {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.randomUUID(),
                "TRANSACTION_CREATED",
                Instant.now(),
                "TXN-123",
                "ACC-123",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                Instant.now()
        );

        consumer.consume(event);

        verify(handler).handle(event);
    }
}