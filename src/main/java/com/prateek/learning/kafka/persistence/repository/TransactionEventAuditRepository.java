package com.prateek.learning.kafka.persistence.repository;

import com.prateek.learning.kafka.persistence.entity.TransactionEventAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionEventAuditRepository extends JpaRepository<TransactionEventAudit, UUID> {
    Optional<TransactionEventAudit> findByEventId(UUID eventId);
}
