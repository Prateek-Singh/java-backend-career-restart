package com.prateek.learning.kafka.event;

import com.prateek.learning.transaction.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionCreatedEvent(
        UUID eventId,
        String eventType,
        Instant eventTimestamp,
        String transactionId,
        String accountId,
        BigDecimal amount,
        TransactionType transactionType,
        Instant transactionCreatedAt
) {
}
