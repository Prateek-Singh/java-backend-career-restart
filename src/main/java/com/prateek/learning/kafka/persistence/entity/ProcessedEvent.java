package com.prateek.learning.kafka.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "PROCESSED_EVENTS")
public class ProcessedEvent {

    @Id
    @Column(name = "EVENT_ID", nullable = false, columnDefinition = "BINARY(16)")
    private UUID eventId;

    @Column(name = "PROCESSED_AT", nullable = false)
    private Instant processedAt;

    @Column(name = "CONSUMER_NAME", nullable = false, length = 55)
    private String consumerName;

    protected ProcessedEvent() {}

    public ProcessedEvent(UUID eventId, Instant processedAt, String consumerName) {
        this.eventId = eventId;
        this.processedAt = processedAt;
        this.consumerName = consumerName;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public String getConsumerName() {
        return consumerName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProcessedEvent that = (ProcessedEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(eventId);
    }

    @Override
    public String toString() {
        return "ProcessedEvent{" +
                "eventId=" + eventId +
                ", processedAt=" + processedAt +
                ", consumerName='" + consumerName + '\'' +
                '}';
    }
}
