package com.prateek.learning.transaction.service;

import com.prateek.learning.transaction.exception.InvalidTransactionAmountException;
import com.prateek.learning.transaction.exception.TransactionNotFoundException;
import com.prateek.learning.transaction.dto.CreateTransactionRequest;
import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.model.TransactionType;
import com.prateek.learning.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    private final List<Transaction> transactions = createSampleTransactions();

    public List<Transaction> findByAccountId(String accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

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

    public Transaction findById(String transactionId) {
        return findById(transactions, transactionId);
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

    private static List<Transaction> createSampleTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        transactions.add(new Transaction(
                "TXN-001",
                "ACC-1001",
                new BigDecimal("25000.00"),
                TransactionType.CREDIT,
                "Monthly Salary",
                LocalDateTime.of(2026, 8, 1, 9, 30)
        ));

        transactions.add(new Transaction(
                "TXN-002",
                "ACC-1001",
                new BigDecimal("-1250.50"),
                TransactionType.DEBIT,
                "Electricity Bill Payment",
                LocalDateTime.of(2026, 8, 1, 11, 15)
        ));

        transactions.add(new Transaction(
                "TXN-003",
                "ACC-1002",
                new BigDecimal("5000.00"),
                TransactionType.CREDIT,
                "Freelance Project PAYMENT",
                LocalDateTime.of(2026, 8, 1, 12, 0)
        ));

        transactions.add(new Transaction(
                "TXN-004",
                "ACC-1002",
                new BigDecimal("-750.25"),
                TransactionType.DEBIT,
                "Online Grocery purchase",
                LocalDateTime.of(2026, 8, 1, 14, 10)
        ));

        transactions.add(new Transaction(
                "TXN-005",
                "ACC-1001",
                new BigDecimal("-2200.00"),
                TransactionType.DEBIT,
                "House RENT",
                LocalDateTime.of(2026, 8, 2, 8, 45)
        ));

        transactions.add(new Transaction(
                "TXN-006",
                "ACC-1002",
                new BigDecimal("1500.75"),
                TransactionType.CREDIT,
                "Cashback Reward",
                LocalDateTime.of(2026, 8, 2, 10, 20)
        ));

        transactions.add(new Transaction(
                "TXN-007",
                "ACC-1001",
                new BigDecimal("-499.99"),
                TransactionType.DEBIT,
                "Streaming Subscription",
                LocalDateTime.of(2026, 8, 2, 13, 5)
        ));

        transactions.add(new Transaction(
                "TXN-008",
                "ACC-1002",
                new BigDecimal("-3000.00"),
                TransactionType.TRANSFER,
                "Transfer to SAVINGS account",
                LocalDateTime.of(2026, 8, 2, 15, 30)
        ));

        transactions.add(new Transaction(
                "TXN-009",
                "ACC-1001",
                new BigDecimal("850.00"),
                TransactionType.REFUND,
                "Refund for Online Purchase",
                LocalDateTime.of(2026, 8, 2, 17, 40)
        ));

        transactions.add(new Transaction(
                "TXN-009", // intentional duplicate ID
                "ACC-1001",
                new BigDecimal("850.00"),
                TransactionType.REFUND,
                "REFUND for online purchase",
                LocalDateTime.of(2026, 8, 2, 17, 40)
        ));

        return transactions;
    }

    public Transaction createTransaction(CreateTransactionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Transaction request is required"
            );
        }

        if (request.id() == null || request.id().isBlank()) {
            throw new IllegalArgumentException(
                    "Transaction id is required"
            );
        }

        if (request.accountId() == null || request.accountId().isBlank()) {
            throw new IllegalArgumentException(
                    "Account id is required"
            );
        }

        if (request.amount() == null) {
            throw new InvalidTransactionAmountException(
                    "Transaction amount is required"
            );
        }

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionAmountException(
                    "Transaction amount must be greater than zero"
            );
        }

        if (request.type() == null) {
            throw new IllegalArgumentException(
                    "Transaction type is required"
            );
        }

        Transaction transaction = new Transaction();
        transaction.setId(request.id());
        transaction.setAccountId(request.accountId());
        transaction.setAmount(request.amount());
        transaction.setType(request.type());
        transaction.setDescription(request.description());
        transaction.setTimestamp(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }

    public Transaction getTransactionById(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("Transaction id is required");
        }

        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }
}
