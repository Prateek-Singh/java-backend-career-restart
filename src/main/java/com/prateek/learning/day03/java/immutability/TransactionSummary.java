package com.prateek.learning.day03.java.immutability;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public final class TransactionSummary {

    private final String accountId;
    private final BigDecimal totalAmount;
    private final List<String> transactionIds;

    public TransactionSummary(
            String accountId,
            BigDecimal totalAmount,
            List<String> transactionIds
    ) {
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

        this.accountId = accountId;
        this.totalAmount = totalAmount;
        this.transactionIds = List.copyOf(transactionIds);
    }

    public String getAccountId() {
        return accountId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<String> getTransactionIds() {
        return transactionIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TransactionSummary that = (TransactionSummary) o;

        return Objects.equals(accountId, that.accountId)
                && Objects.equals(totalAmount, that.totalAmount)
                && Objects.equals(transactionIds, that.transactionIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                accountId,
                totalAmount,
                transactionIds
        );
    }

    @Override
    public String toString() {
        return "TransactionSummary{" +
                "accountId='" + accountId + '\'' +
                ", totalAmount=" + totalAmount +
                ", transactionIds=" + transactionIds +
                '}';
    }
}