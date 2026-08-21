package com.prateek.learning.kafka.integration;

import com.prateek.learning.kafka.consumer.handler.TransactionCreatedEventHandler;
import com.prateek.learning.kafka.event.TransactionCreatedEvent;
import com.prateek.learning.kafka.producer.TransactionEventPublisher;
import com.prateek.learning.transaction.model.TransactionType;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class KafkaRetryDltIntegrationTest {

    private static final String SOURCE_TOPIC =
            "transaction-events";

    private static final String DLT_TOPIC =
            "transaction-events-dlt";

    @Container
    static final KafkaContainer kafka =
            new KafkaContainer(
                    "apache/kafka-native:4.3.1"
            );

    @DynamicPropertySource
    static void kafkaProperties(
            DynamicPropertyRegistry registry) {

        registry.add(
                "spring.kafka.bootstrap-servers",
                kafka::getBootstrapServers
        );

        registry.add(
                "spring.kafka.producer.key-serializer",
                () ->
                        "org.apache.kafka.common.serialization.StringSerializer"
        );

        registry.add(
                "spring.kafka.producer.value-serializer",
                () ->
                        "org.springframework.kafka.support.serializer.JsonSerializer"
        );

        registry.add(
                "spring.kafka.consumer.key-deserializer",
                () ->
                        "org.apache.kafka.common.serialization.StringDeserializer"
        );

        registry.add(
                "spring.kafka.consumer.value-deserializer",
                () ->
                        "org.springframework.kafka.support.serializer.JsonDeserializer"
        );

        registry.add(
                "spring.kafka.consumer.properties.spring.json.trusted.packages",
                () ->
                        "com.prateek.learning.kafka.event"
        );

        registry.add(
                "spring.kafka.consumer.properties.spring.json.value.default.type",
                () ->
                        "com.prateek.learning.kafka.event.TransactionCreatedEvent"
        );

        registry.add(
                "spring.kafka.consumer.properties.spring.json.use.type.headers",
                () -> "false"
        );

        registry.add(
                "spring.kafka.consumer.auto-offset-reset",
                () -> "earliest"
        );
    }

    @Autowired
    private TransactionEventPublisher publisher;

    @MockitoBean
    private TransactionCreatedEventHandler handler;

    @Test
    void shouldPublishToDltAfterRetryableFailure() {

        String transactionId =
                "TXN-RETRY-" + UUID.randomUUID();

        TransactionCreatedEvent event =
                new TransactionCreatedEvent(
                        UUID.randomUUID(),
                        "TRANSACTION_CREATED",
                        Instant.parse("2026-08-17T07:00:00Z"),
                        transactionId,
                        "ACC-RETRY-1",
                        new BigDecimal("100.00"),
                        TransactionType.CREDIT,
                        Instant.parse("2026-08-17T06:59:00Z")
                );

        doThrow(
                new RuntimeException(
                        "temporary database failure"
                )
        )
                .when(handler)
                .handle(any(TransactionCreatedEvent.class));

        publisher.publish(event);

        ConsumerRecord<String, byte[]> dltRecord =
                consumeDltRecord(transactionId);

        assertThat(dltRecord.key())
                .isEqualTo(transactionId);

        assertThat(dltRecord.value())
                .isNotEmpty();

        assertDltHeaders(dltRecord);
    }

    @Test
    void shouldPublishToDltForValidationFailureWithoutCallingHandler() {

        /*
         * Keep this test independent from any invocation that may
         * have happened in another test method.
         */
        clearInvocations(handler);

        String transactionId =
                "TXN-INVALID-" + UUID.randomUUID();

        TransactionCreatedEvent event =
                new TransactionCreatedEvent(
                        UUID.randomUUID(),
                        "TRANSACTION_CREATED",
                        Instant.parse("2026-08-17T07:00:00Z"),
                        transactionId,
                        "",
                        new BigDecimal("100.00"),
                        TransactionType.CREDIT,
                        Instant.parse("2026-08-17T06:59:00Z")
                );

        publisher.publish(event);

        ConsumerRecord<String, byte[]> dltRecord =
                consumeDltRecord(transactionId);

        verifyNoInteractions(handler);

        assertThat(dltRecord.key())
                .isEqualTo(transactionId);

        assertThat(dltRecord.value())
                .isNotEmpty();

        assertDltHeaders(dltRecord);
    }

    private ConsumerRecord<String, byte[]> consumeDltRecord(
            String expectedKey) {

        Map<String, Object> properties =
                new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafka.getBootstrapServers()
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "retry-dlt-test-" + UUID.randomUUID()
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        try (var consumer =
                     new DefaultKafkaConsumerFactory<>(
                             properties,
                             new StringDeserializer(),
                             new ByteArrayDeserializer()
                     ).createConsumer()) {

            consumer.subscribe(
                    List.of(DLT_TOPIC)
            );

            long deadline =
                    System.currentTimeMillis()
                            + 25_000;

            while (System.currentTimeMillis() < deadline) {

                var records =
                        consumer.poll(
                                Duration.ofMillis(500)
                        );

                for (var record : records) {

                    if (expectedKey.equals(
                            record.key()
                    )) {
                        return record;
                    }
                }
            }

            throw new AssertionError(
                    "Expected DLT record not found for key: "
                            + expectedKey
            );
        }
    }

    private void assertDltHeaders(
            ConsumerRecord<String, byte[]> dltRecord) {

        Header originalTopicHeader =
                dltRecord.headers()
                        .lastHeader(
                                KafkaHeaders.DLT_ORIGINAL_TOPIC
                        );

        Header originalPartitionHeader =
                dltRecord.headers()
                        .lastHeader(
                                KafkaHeaders.DLT_ORIGINAL_PARTITION
                        );

        Header exceptionFqcnHeader =
                dltRecord.headers()
                        .lastHeader(
                                KafkaHeaders.DLT_EXCEPTION_FQCN
                        );

        Header exceptionMessageHeader =
                dltRecord.headers()
                        .lastHeader(
                                KafkaHeaders.DLT_EXCEPTION_MESSAGE
                        );

        assertThat(originalTopicHeader)
                .isNotNull();

        assertThat(originalPartitionHeader)
                .isNotNull();

        assertThat(exceptionFqcnHeader)
                .isNotNull();

        assertThat(exceptionMessageHeader)
                .isNotNull();

        String originalTopic =
                new String(
                        originalTopicHeader.value(),
                        StandardCharsets.UTF_8
                );

        assertThat(originalTopic)
                .isEqualTo(SOURCE_TOPIC);

        int originalPartition =
                ByteBuffer
                        .wrap(
                                originalPartitionHeader.value()
                        )
                        .getInt();

        assertThat(originalPartition)
                .isEqualTo(
                        dltRecord.partition()
                );

        String exceptionFqcn =
                new String(
                        exceptionFqcnHeader.value(),
                        StandardCharsets.UTF_8
                );

        String exceptionMessage =
                new String(
                        exceptionMessageHeader.value(),
                        StandardCharsets.UTF_8
                );

        assertThat(exceptionFqcn)
                .isNotBlank();

        assertThat(exceptionMessage)
                .isNotBlank();
    }
}