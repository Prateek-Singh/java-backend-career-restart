package com.prateek.learning.kafka.processing;

import com.prateek.learning.kafka.event.TransactionCreatedEvent;
import com.prateek.learning.kafka.persistence.entity.ProcessedEvent;
import com.prateek.learning.kafka.persistence.entity.TransactionEventAudit;
import com.prateek.learning.kafka.persistence.repository.ProcessedEventRepository;
import com.prateek.learning.kafka.persistence.repository.TransactionEventAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class TransactionEventProcessingService {

    private static final String CONSUMER_NAME = "transaction-created-events-cg";

    private final ProcessedEventRepository processedEventRepository;
    private final TransactionEventAuditRepository transactionEventAuditRepository;

    public TransactionEventProcessingService(
            ProcessedEventRepository processedEventRepository,
            TransactionEventAuditRepository transactionEventAuditRepository) {
        this.processedEventRepository = processedEventRepository;
        this.transactionEventAuditRepository = transactionEventAuditRepository;
    }

    @Transactional
    public void process(TransactionCreatedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            return;
        }
        ProcessedEvent eventEntity = mapEventToEntity(event);
        TransactionEventAudit auditEntity = mapEventToAuditEntity(event);
        processedEventRepository.saveAndFlush(eventEntity);
        transactionEventAuditRepository.save(auditEntity);
    }

    private TransactionEventAudit mapEventToAuditEntity(TransactionCreatedEvent event) {
        return new TransactionEventAudit(
                event.eventId(),
                event.transactionId(),
                event.accountId(),
                event.amount(),
                event.transactionType(),
                event.eventTimestamp(),
                Instant.now()
        );
    }

    private ProcessedEvent mapEventToEntity(TransactionCreatedEvent event) {
        return new ProcessedEvent(
                event.eventId(),
                Instant.now(),
                CONSUMER_NAME
        );
    }
}
