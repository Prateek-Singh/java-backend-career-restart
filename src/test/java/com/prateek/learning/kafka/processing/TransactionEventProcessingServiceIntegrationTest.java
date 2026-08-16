package com.prateek.learning.kafka.processing;

import com.prateek.learning.kafka.event.TransactionCreatedEvent;
import com.prateek.learning.kafka.persistence.entity.ProcessedEvent;
import com.prateek.learning.kafka.persistence.entity.TransactionEventAudit;
import com.prateek.learning.kafka.persistence.repository.ProcessedEventRepository;
import com.prateek.learning.kafka.persistence.repository.TransactionEventAuditRepository;
import com.prateek.learning.transaction.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransactionEventProcessingServiceIntegrationTest {

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private TransactionEventAuditRepository transactionEventAuditRepository;

    @Autowired
    private TransactionEventProcessingService transactionEventProcessingService;

    @BeforeEach
    void setUp() {
        transactionEventAuditRepository.deleteAll();
        processedEventRepository.deleteAll();
    }

    @Test
    void shouldSaveNewEvent() {

        UUID eventId = UUID.randomUUID();
        Instant eventTimestamp = Instant.now();
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                eventId,
                "TRANSACTION_CREATED",
                eventTimestamp,
                "TXN-111",
                "ACC-111",
                new BigDecimal("25.00"),
                TransactionType.CREDIT,
                Instant.now()
        );

        transactionEventProcessingService.process(event);

        ProcessedEvent processedEvent = processedEventRepository.findById(eventId).orElseThrow();

        TransactionEventAudit transactionEventAudit = transactionEventAuditRepository.findByEventId(eventId).orElseThrow();

        assertEquals(1, processedEventRepository.count());
        assertEquals(1, transactionEventAuditRepository.count());

        assertEquals(eventId, processedEvent.getEventId());
        assertEquals("transaction-created-events-cg", processedEvent.getConsumerName());
        assertNotNull(processedEvent.getProcessedAt());

        assertEquals(eventId, transactionEventAudit.getEventId());
        assertEquals("TXN-111", transactionEventAudit.getTransactionId());
        assertEquals("ACC-111", transactionEventAudit.getAccountId());
        assertEquals(new BigDecimal("25.00"), transactionEventAudit.getAmount());
        assertEquals(TransactionType.CREDIT, transactionEventAudit.getType());
        assertEquals(eventTimestamp, transactionEventAudit.getEventTimestamp());
        assertNotNull(transactionEventAudit.getCreatedAt());
    }

    @Test
    void shouldHandleDuplicateEvent() {

        UUID eventId = UUID.randomUUID();
        Instant eventTimestamp = Instant.now();
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                eventId,
                "TRANSACTION_CREATED",
                eventTimestamp,
                "TXN-111",
                "ACC-111",
                new BigDecimal("25.00"),
                TransactionType.CREDIT,
                Instant.now()
        );

        transactionEventProcessingService.process(event);
        assertThatCode(
                () -> transactionEventProcessingService.process(event)
        ).doesNotThrowAnyException();

        assertEquals(1, processedEventRepository.count());
        assertEquals(1, transactionEventAuditRepository.count());
        assertTrue(processedEventRepository.existsById(eventId));
    }

    @Test
    void shouldRollbackEventTransaction() {

        UUID eventId = UUID.randomUUID();
        Instant eventTimestamp = Instant.now();
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                eventId,
                "TRANSACTION_CREATED",
                eventTimestamp,
            "TXN-ROLLBACK",
                null,
                new BigDecimal("25.00"),
                TransactionType.CREDIT,
                Instant.now()
        );

        assertThatThrownBy(
                () -> transactionEventProcessingService.process(event)
        ).isInstanceOf(DataIntegrityViolationException.class);

        assertThat(processedEventRepository.count()).isZero();
        assertThat(transactionEventAuditRepository.count()).isZero();
    }
}