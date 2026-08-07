package com.prateek.learning.transaction.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction {
    private String id;
    private String accountId;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private Instant timestamp;

    // constructors, getters, setters, equals, hashCode, toString

    public Transaction() {
    }

    public Transaction(String id, String accountId, BigDecimal amount, TransactionType type, String description, Instant timestamp) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id) && Objects.equals(accountId, that.accountId) && Objects.equals(amount, that.amount) && Objects.equals(type, that.type) && Objects.equals(description, that.description) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountId, amount, type, description, timestamp);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", accountId='" + accountId + '\'' +
                ", amount=" + amount +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}