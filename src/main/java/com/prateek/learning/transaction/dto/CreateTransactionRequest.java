package com.prateek.learning.transaction.dto;

import java.math.BigDecimal;

public record CreateTransactionRequest(
        String id,
        String accountId,
        BigDecimal amount,
        String type,
        String description
) {
}
