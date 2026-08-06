package com.prateek.learning.transaction.repository;

import com.prateek.learning.transaction.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryTransactionRepository implements  TransactionRepository {

    private final Map<String, Transaction> transactions = new HashMap<>();

    @Override
    public Transaction save(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        if (transaction.getId() == null || transaction.getId().isBlank()) {
            throw new IllegalArgumentException("Transaction id cannot be null or empty");
        }
        if (transactions.containsKey(transaction.getId())) {
            throw new IllegalArgumentException("Transaction already exists");
        }
        transactions.put(transaction.getId(), transaction);
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("Transaction id cannot be null or empty");
        }
        return Optional.ofNullable(transactions.get(transactionId));
    }

    @Override
    public List<Transaction> findByAccountId(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account id cannot be null or empty");
        }
        return transactions
                .values()
                .stream()
                .filter(txn -> accountId.equals(txn.getAccountId()))
                .toList();
    }

    @Override
    public void clear() {
        transactions.clear();
    }
}
