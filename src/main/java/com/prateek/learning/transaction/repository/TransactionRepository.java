package com.prateek.learning.transaction.repository;

import com.prateek.learning.transaction.model.Transaction;

import java.util.Optional;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findById(String transactionId);
}