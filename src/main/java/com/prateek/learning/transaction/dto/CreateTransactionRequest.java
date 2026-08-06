package com.prateek.learning.transaction.dto;

import com.prateek.learning.transaction.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateTransactionRequest(

        @NotBlank(message = "id cannot be null or blank")
        String id,

        @NotBlank(message = "accountId cannot be null or blank")
        String accountId,

        @NotNull(message = "amount cannot be null")
        @Positive(message = "amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "type cannot be null")
        TransactionType type,

        @NotBlank(message = "description cannot be null or blank")
        String description
) {
}
