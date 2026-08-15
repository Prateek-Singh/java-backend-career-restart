package com.prateek.learning.kafka.integration;

import com.prateek.learning.kafka.consumer.handler.TransactionCreatedEventHandler;
import com.prateek.learning.kafka.event.TransactionCreatedEvent;
import com.prateek.learning.kafka.producer.TransactionEventPublisher;
import com.prateek.learning.transaction.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

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

    @MockitoBean
    private TransactionCreatedEventHandler transactionCreatedEventHandler;

    @Test
    void shouldPublishAndReceiveTransactionCreatedEvent() {

        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "TRANSACTION_CREATED",
                Instant.parse("2026-08-13T08:01:00Z"),
                "TXN-500",
                "ACC-500",
                new BigDecimal("25.00"),
                TransactionType.CREDIT,
                Instant.parse("2026-08-13T08:00:00Z")
        );

        transactionEventPublisher.publish(event);

        verify(transactionCreatedEventHandler, timeout(10_000))
                .handle(event);
    }
}
