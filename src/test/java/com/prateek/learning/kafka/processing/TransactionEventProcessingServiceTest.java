package com.prateek.learning.kafka.processing;

import com.prateek.learning.kafka.event.TransactionCreatedEvent;
import com.prateek.learning.kafka.persistence.entity.ProcessedEvent;
import com.prateek.learning.kafka.persistence.entity.TransactionEventAudit;
import com.prateek.learning.kafka.persistence.repository.ProcessedEventRepository;
import com.prateek.learning.kafka.persistence.repository.TransactionEventAuditRepository;
import com.prateek.learning.transaction.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionEventProcessingServiceTest {

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private TransactionEventAuditRepository transactionEventAuditRepository;

    @InjectMocks
    private TransactionEventProcessingService transactionEventProcessingService;

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

        when(processedEventRepository.existsById(eq(eventId))).thenReturn(Boolean.FALSE);

        transactionEventProcessingService.process(event);

        verify(processedEventRepository).existsById(eventId);

        ArgumentCaptor<ProcessedEvent> processedEventCaptor = ArgumentCaptor.forClass(ProcessedEvent.class);
        ArgumentCaptor<TransactionEventAudit> transactionEventAuditArgumentCaptor = ArgumentCaptor.forClass(TransactionEventAudit.class);

        verify(processedEventRepository).saveAndFlush(processedEventCaptor.capture());
        verify(transactionEventAuditRepository).save(transactionEventAuditArgumentCaptor.capture());

        assertThat(processedEventCaptor.getValue().getEventId()).isEqualTo(eventId);
        assertThat(processedEventCaptor.getValue().getConsumerName()).isEqualTo("transaction-created-events-cg");

        assertThat(transactionEventAuditArgumentCaptor.getValue().getEventId()).isEqualTo(eventId);
        assertThat(transactionEventAuditArgumentCaptor.getValue().getTransactionId()).isEqualTo("TXN-111");
        assertThat(transactionEventAuditArgumentCaptor.getValue().getAccountId()).isEqualTo("ACC-111");
        assertThat(transactionEventAuditArgumentCaptor.getValue().getAmount()).isEqualTo(new BigDecimal("25.00"));
        assertThat(transactionEventAuditArgumentCaptor.getValue().getType()).isEqualTo(TransactionType.CREDIT);
        assertThat(transactionEventAuditArgumentCaptor.getValue().getEventTimestamp()).isEqualTo(eventTimestamp);

        assertThat(processedEventCaptor.getValue().getProcessedAt()).isNotNull();
        assertThat(transactionEventAuditArgumentCaptor.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldNotPersistDuplicateEvent() {
        UUID eventId = UUID.randomUUID();
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                eventId,
                "TRANSACTION_CREATED",
                Instant.now(),
                "TXN-111",
                "ACC-111",
                new BigDecimal("25.00"),
                TransactionType.CREDIT,
                Instant.now()
        );

        when(processedEventRepository.existsById(eq(eventId))).thenReturn(Boolean.TRUE);

        transactionEventProcessingService.process(event);

        verify(processedEventRepository).existsById(eventId);

        verifyNoMoreInteractions(processedEventRepository);
        verifyNoInteractions(transactionEventAuditRepository);

    }
}