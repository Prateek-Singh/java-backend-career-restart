package com.prateek.learning.kafka.producer;

import com.prateek.learning.kafka.event.TransactionCreatedEvent;
import com.prateek.learning.transaction.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionEventPublisherTest {

    @InjectMocks
    private TransactionEventPublisher transactionEventPublisher;

    @Mock
    private KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

    @Test
    void shouldPublishTransactionCreatedEvent() {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.randomUUID(),
                "TRANSACTION_CREATED",
                Instant.now(),
                "TXN-100",
                "ACC-100",
                new BigDecimal("25.00"),
                TransactionType.CREDIT,
                Instant.now()
        );

        transactionEventPublisher.publish(event);

        verify(kafkaTemplate).send("transaction-events", event.transactionId(), event);
    }
}