package com.prateek.learning.day01.java;

import com.prateek.learning.day03.java.exceptions.InvalidTransactionAmountException;
import com.prateek.learning.day03.java.exceptions.TransactionNotFoundException;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionService {

    List<Transaction> findByAccountId(List<Transaction> transactions, String accountId) {
        if (transactions == null || transactions.isEmpty() || accountId == null || accountId.isBlank()) {
            return List.of();
        }

        return transactions.stream()
                .filter(Objects::nonNull)
                .filter(txn -> accountId.equals(txn.getAccountId()))
                .toList();
    }

    BigDecimal calculateTotalAmount(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return transactions.stream()
                .filter(Objects::nonNull)
                .map(Transaction::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    Map<String, List<Transaction>> groupByType(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return Map.of();
        }

        return transactions.stream()
                .filter(Objects::nonNull)
                .filter(txn -> txn.getType() != null)
                .collect(Collectors.groupingBy(Transaction::getType));
    }

    Optional<Transaction> findHighestAmount(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return Optional.empty();
        }

        return transactions
                .stream()
                .filter(Objects::nonNull)
                .filter(txn -> txn.getAmount() != null)
                .max(Comparator.comparing(Transaction::getAmount));
    }

    List<Transaction> findByDescriptionKeyword(
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

    List<Transaction> removeDuplicatesById(List<Transaction> transactions) {
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

    List<Transaction> sortByAmountDescending(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return List.of();
        }
        // if null amounts need to be ignored
        return transactions.stream()
                .filter(Objects::nonNull)
                .filter(txn -> txn.getAmount() != null)
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .toList();

        // if null amounts need to be considered but appended last
//        return transactions.stream()
//                .filter(Objects::nonNull)
//                .filter(txn -> txn.getAmount() != null)
//                .sorted(
//                        Comparator.comparing(
//                                Transaction::getAmount,
//                                Comparator.nullsLast(Comparator.naturalOrder())
//                        ).reversed()
//                )
//                .toList();

    }

    Map<String, BigDecimal> calculateTotalAmountPerAccount(
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
        //With toMap
//        return transactions
//                .stream()
//                .filter(Objects::nonNull)
//                .filter(txn -> txn.getAccountId() != null && txn.getAmount() != null)
//                .collect(Collectors.toMap(
//                Transaction::getAccountId,
//                Transaction::getAmount,
//                BigDecimal::add));
    }

    public Transaction findById(
            List<Transaction> transactions,
            String transactionId
    ) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("Transaction ID is required");
        }

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

    public void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidTransactionAmountException(
                    "Transaction amount is required"
            );
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionAmountException(
                    "Transaction amount must be greater than zero"
            );
        }
    }
}
