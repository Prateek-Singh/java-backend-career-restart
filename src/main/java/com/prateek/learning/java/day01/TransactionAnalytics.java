package com.prateek.learning.java.day01;

import com.prateek.learning.transaction.exception.TransactionNotFoundException;
import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.model.TransactionType;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionAnalytics {


    public List<Transaction> findByAccountId(List<Transaction> transactions, String accountId) {
        if (transactions == null || transactions.isEmpty() || accountId == null || accountId.isBlank()) {
            return List.of();
        }

        return transactions.stream()
                .filter(Objects::nonNull)
                .filter(txn -> accountId.equals(txn.getAccountId()))
                .toList();
    }

    public BigDecimal calculateTotalAmount(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return transactions.stream()
                .filter(Objects::nonNull)
                .map(Transaction::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<TransactionType, List<Transaction>> groupByType(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return Map.of();
        }

        return transactions.stream()
                .filter(Objects::nonNull)
                .filter(txn -> txn.getType() != null)
                .collect(Collectors.groupingBy(Transaction::getType));
    }

    public Optional<Transaction> findHighestAmount(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return Optional.empty();
        }

        return transactions
                .stream()
                .filter(Objects::nonNull)
                .filter(txn -> txn.getAmount() != null)
                .max(Comparator.comparing(Transaction::getAmount));
    }

    public List<Transaction> findByDescriptionKeyword(
            List<Transaction> transactions,
            String keyword
    ) {
        if (transactions == null
                || transactions.isEmpty()
                || keyword == null
                || keyword.isBlank()) {
            return List.of();
        }

        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);

        return transactions.stream()
                .filter(Objects::nonNull)
                .filter(txn -> txn.getDescription() != null)
                .filter(txn -> txn.getDescription()
                        .toLowerCase(Locale.ROOT)
                        .contains(normalizedKeyword))
                .toList();
    }

    public List<Transaction> removeDuplicatesById(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return List.of();
        }

        Set<String> seenIds = new HashSet<>();

        return transactions.stream()
                .filter(Objects::nonNull)
                .filter(txn -> txn.getId() != null)
                .filter(txn -> seenIds.add(txn.getId()))
                .toList();
    }

    public List<Transaction> sortByAmountDescending(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return List.of();
        }
        // if null amounts need to be ignored
        return transactions.stream()
                .filter(Objects::nonNull)
                .filter(txn -> txn.getAmount() != null)
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .toList();
    }

    public Map<String, BigDecimal> calculateTotalAmountPerAccount(
            List<Transaction> transactions
    ) {
        if (transactions == null || transactions.isEmpty()) {
            return Map.of();
        }

        //With grouping and reducing
        return transactions
                .stream()
                .filter(Objects::nonNull)
                .filter(txn -> txn.getAccountId() != null && txn.getAmount() != null)
                .collect(Collectors.groupingBy(
                        Transaction::getAccountId,
                        Collectors.reducing(BigDecimal.ZERO,
                                Transaction::getAmount, BigDecimal::add)));
    }

    public Transaction findById(
            List<Transaction> transactions,
            String transactionId
    ) {
        validateTransactionId(transactionId);

        if (transactions == null || transactions.isEmpty()) {
            throw new TransactionNotFoundException(
                    "Transaction not found for ID: " + transactionId
            );
        }

        return transactions.stream()
                .filter(Objects::nonNull)
                .filter(txn -> transactionId.equals(txn.getId()))
                .findFirst()
                .orElseThrow(() ->
                        new TransactionNotFoundException(
                                "Transaction not found for ID: " + transactionId
                        )
                );
    }

    private void validateTransactionId(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("Transaction id is required");
        }
    }

}
