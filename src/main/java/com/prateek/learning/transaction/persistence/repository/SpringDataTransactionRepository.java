package com.prateek.learning.transaction.persistence.repository;

import com.prateek.learning.transaction.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataTransactionRepository
        extends JpaRepository<TransactionEntity, UUID> {

    Optional<TransactionEntity> findByTransactionId(String transactionId);

    List<TransactionEntity> findByAccountId(String accountId);
}