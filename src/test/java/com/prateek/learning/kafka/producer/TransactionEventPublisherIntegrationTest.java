package com.prateek.learning.kafka.producer;

import com.prateek.learning.kafka.event.TransactionCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.util.HashMap;
import java.util.Map;

import com.prateek.learning.transaction.model.TransactionType;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("jpa")
class TransactionEventPublisherIntegrationTest {

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
    }

    @Autowired
    private TransactionEventPublisher transactionEventPublisher;

    private ConsumerFactory<String, TransactionCreatedEvent> createConsumerFactory() {
        Map<String, Object> consumerProps = new HashMap<>();

        consumerProps.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafka.getBootstrapServers()
        );
        consumerProps.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "transaction-event-publisher-it"
        );
        consumerProps.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        JsonDeserializer<TransactionCreatedEvent> valueDeserializer =
                new JsonDeserializer<>(TransactionCreatedEvent.class);

        valueDeserializer.addTrustedPackages(
                "com.prateek.learning.kafka.event"
        );

        return new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Test
    void shouldPublishTransactionCreatedEventToKafka() {
        ConsumerFactory<String, TransactionCreatedEvent> consumerFactory =
                createConsumerFactory();

        try (Consumer<String, TransactionCreatedEvent> consumer =
                     consumerFactory.createConsumer()) {

            consumer.subscribe(List.of("transaction-events"));

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

            ConsumerRecords<String, TransactionCreatedEvent> records =
                    consumer.poll(Duration.ofSeconds(10));

            assertFalse(records.isEmpty());

            ConsumerRecord<String, TransactionCreatedEvent> record =
                    records.iterator().next();

            assertEquals("TXN-500", record.key());
            assertEquals(event, record.value());
        }
    }
}