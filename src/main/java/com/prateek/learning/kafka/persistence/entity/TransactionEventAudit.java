package com.prateek.learning.kafka.persistence.entity;

import com.prateek.learning.transaction.model.TransactionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "TRANSACTION_EVENT_AUDIT")
public class TransactionEventAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "EVENT_ID", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private UUID eventId;

    @Column(name = "TRANSACTION_ID", nullable = false, length = 255)
    private String transactionId;

    @Column(name = "ACCOUNT_ID", nullable = false, length = 255)
    private String accountId;

    @Column(name = "AMOUNT", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "TRANSACTION_TYPE", nullable = false, length = 50)
    private TransactionType type;

    @Column(name = "EVENT_TIMESTAMP", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    protected TransactionEventAudit() {}

    public TransactionEventAudit(UUID eventId, String transactionId, String accountId, BigDecimal amount, TransactionType type, Instant eventTimestamp, Instant createdAt) {
        this.eventId = eventId;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
        this.eventTimestamp = eventTimestamp;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public Instant getEventTimestamp() {
        return eventTimestamp;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TransactionEventAudit that = (TransactionEventAudit) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(eventId);
    }

    @Override
    public String toString() {
        return "TransactionEventAudit{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", transactionId='" + transactionId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", amount=" + amount +
                ", type=" + type +
                ", eventTimestamp=" + eventTimestamp +
                ", createdAt=" + createdAt +
                '}';
    }
}
