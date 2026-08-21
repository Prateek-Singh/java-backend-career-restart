package com.prateek.learning.kafka.event;

import com.prateek.learning.transaction.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionCreatedEvent(
        @NotNull
        UUID eventId,

        @NotBlank
        String eventType,

        @NotNull
        Instant eventTimestamp,

        @NotBlank
        String transactionId,

        @NotBlank
        String accountId,

        @NotNull
        @Positive
        BigDecimal amount,

        @NotNull
        TransactionType transactionType,

        @NotNull
        Instant transactionCreatedAt
) {
}
