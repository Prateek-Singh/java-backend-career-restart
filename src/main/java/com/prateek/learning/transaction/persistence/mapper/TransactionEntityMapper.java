package com.prateek.learning.transaction.persistence.mapper;

import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionEntityMapper {

    public TransactionEntity toEntity(Transaction transaction) {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setTransactionId(transaction.getId());
        transactionEntity.setAccountId(transaction.getAccountId());
        transactionEntity.setAmount(transaction.getAmount());
        transactionEntity.setType(transaction.getType());
        transactionEntity.setDescription(transaction.getDescription());
        transactionEntity.setCreatedTimestamp(transaction.getTimestamp());
        return transactionEntity;
    }

    public Transaction toDomain(TransactionEntity entity) {
        Transaction transaction = new Transaction();
        transaction.setId(entity.getTransactionId());
        transaction.setAccountId(entity.getAccountId());
        transaction.setAmount(entity.getAmount());
        transaction.setType(entity.getType());
        transaction.setDescription(entity.getDescription());
        transaction.setTimestamp(entity.getCreatedTimestamp());
        return transaction;
    }
}
