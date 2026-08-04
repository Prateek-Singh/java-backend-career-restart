package com.prateek.learning.day04.java.springboot.dto;

import java.math.BigDecimal;

public record CreateTransactionRequest(
        String id,
        String accountId,
        BigDecimal amount,
        String type,
        String description
) {
}
