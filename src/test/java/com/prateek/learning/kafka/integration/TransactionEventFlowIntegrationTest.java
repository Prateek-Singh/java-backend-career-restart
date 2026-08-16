package com.prateek.learning.kafka.integration;

import com.prateek.learning.kafka.event.TransactionCreatedEvent;
import com.prateek.learning.kafka.persistence.entity.TransactionEventAudit;
import com.prateek.learning.kafka.persistence.repository.ProcessedEventRepository;
import com.prateek.learning.kafka.persistence.repository.TransactionEventAuditRepository;
import com.prateek.learning.kafka.producer.TransactionEventPublisher;
import com.prateek.learning.transaction.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
class TransactionEventFlowIntegrationTest {

    @Container
    static final KafkaContainer kafka =
            new KafkaContainer("apache/kafka-native:4.3.1");

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.kafka.bootstrap-servers",
                kafka::getBootstrapServers
        );

        registry.add(
                "spring.kafka.producer.key-serializer",
                () -> "org.apache.kafka.common.serialization.StringSerializer"
        );

        registry.add(
                "spring.kafka.producer.value-serializer",
                () -> "org.springframework.kafka.support.serializer.JsonSerializer"
        );

        registry.add(
                "spring.kafka.consumer.key-deserializer",
                () -> "org.apache.kafka.common.serialization.StringDeserializer"
        );

        registry.add(
                "spring.kafka.consumer.value-deserializer",
                () -> "org.springframework.kafka.support.serializer.JsonDeserializer"
        );

        registry.add(
                "spring.kafka.consumer.properties.spring.json.trusted.packages",
                () -> "com.prateek.learning.kafka.event"
        );

        registry.add(
                "spring.kafka.consumer.properties.spring.json.value.default.type",
                () -> "com.prateek.learning.kafka.event.TransactionCreatedEvent"
        );

        registry.add(
                "spring.kafka.consumer.auto-offset-reset",
                () -> "earliest"
        );
    }

    @Autowired
    private TransactionEventPublisher transactionEventPublisher;

    @Autowired
    private TransactionEventAuditRepository transactionEventAuditRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @BeforeEach
    void setUp() {
        transactionEventAuditRepository.deleteAll();
        processedEventRepository.deleteAll();
    }

    @Test
    void shouldPublishAndReceiveTransactionCreatedEvent() {

        UUID eventId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                eventId,
                "TRANSACTION_CREATED",
                Instant.parse("2026-08-13T08:01:00Z"),
                "TXN-500",
                "ACC-500",
                new BigDecimal("25.00"),
                TransactionType.CREDIT,
                Instant.parse("2026-08-13T08:00:00Z")
        );

        transactionEventPublisher.publish(event);

        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    assertThat(processedEventRepository.findById(eventId))
                            .isPresent();

                    TransactionEventAudit audit =
                            transactionEventAuditRepository.findByEventId(eventId)
                                    .orElseThrow();

                    assertThat(audit.getTransactionId()).isEqualTo("TXN-500");
                    assertThat(audit.getAccountId()).isEqualTo("ACC-500");
                    assertThat(audit.getAmount()).isEqualByComparingTo("25.00");
                    assertThat(audit.getType()).isEqualTo(TransactionType.CREDIT);
                });
    }

    @Test
    void shouldHandleDuplicateTransactionCreatedEvent() {

        UUID eventId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                eventId,
                "TRANSACTION_CREATED",
                Instant.parse("2026-08-13T08:01:00Z"),
                "TXN-500",
                "ACC-500",
                new BigDecimal("25.00"),
                TransactionType.CREDIT,
                Instant.parse("2026-08-13T08:00:00Z")
        );

        transactionEventPublisher.publish(event);

        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    assertThat(processedEventRepository.count()).isEqualTo(1);
                    assertThat(transactionEventAuditRepository.count()).isEqualTo(1);
                });

        transactionEventPublisher.publish(event);

        await()
                .atMost(Duration.ofSeconds(10))
                .during(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    assertThat(processedEventRepository.count()).isEqualTo(1);
                    assertThat(transactionEventAuditRepository.count()).isEqualTo(1);
                });
    }
}
