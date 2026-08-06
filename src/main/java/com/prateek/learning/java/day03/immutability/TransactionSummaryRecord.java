package com.prateek.learning.java.day03.immutability;

import java.math.BigDecimal;
import java.util.List;

public record TransactionSummaryRecord(
        String accountId,
        BigDecimal totalAmount,
        List<String> transactionIds
) {
    public TransactionSummaryRecord {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException(
                    "accountId cannot be null or blank"
            );
        }

        if (totalAmount == null) {
            throw new IllegalArgumentException(
                    "totalAmount cannot be null"
            );
        }

        if (transactionIds == null) {
            throw new IllegalArgumentException(
                    "transactionIds cannot be null"
            );
        }

        transactionIds = List.copyOf(transactionIds);
    }
}
