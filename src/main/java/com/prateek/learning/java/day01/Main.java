package com.prateek.learning.java.day01;

import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.model.TransactionType;
import com.prateek.learning.transaction.repository.InMemoryTransactionRepository;
import com.prateek.learning.transaction.repository.TransactionRepository;
import com.prateek.learning.transaction.service.TransactionService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        TransactionAnalytics transactionAnalytics = new TransactionAnalytics();

        List<Transaction> transactions = createSampleTransactions();
        List<Transaction> emptyTransactions = List.of();

        BigDecimal calculateTotalAmount = transactionAnalytics.calculateTotalAmount(transactions);
        System.out.println("Total amount: " + calculateTotalAmount);

        List<Transaction> transaction = transactionAnalytics.findByAccountId(transactions, "ACC-1001");
        System.out.println("Transaction Id: " + transaction.get(0).getId() + " Id : " + transaction.get(1).getId());
    }

    private static List<Transaction> createSampleTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        transactions.add(new Transaction(
                "TXN-001",
                "ACC-1001",
                new BigDecimal("25000.00"),
                TransactionType.CREDIT,
                "Monthly Salary",
                Instant.now()
        ));

        transactions.add(new Transaction(
                "TXN-002",
                "ACC-1001",
                new BigDecimal("-1250.50"),
                TransactionType.DEBIT,
                "Electricity Bill Payment",
                Instant.now()
        ));

        transactions.add(new Transaction(
                "TXN-003",
                "ACC-1002",
                new BigDecimal("5000.00"),
                TransactionType.CREDIT,
                "Freelance Project PAYMENT",
                Instant.now()
        ));

        transactions.add(new Transaction(
                "TXN-004",
                "ACC-1002",
                new BigDecimal("-750.25"),
                TransactionType.DEBIT,
                "Online Grocery purchase",
                Instant.now()
        ));

        transactions.add(new Transaction(
                "TXN-005",
                "ACC-1001",
                new BigDecimal("-2200.00"),
                TransactionType.DEBIT,
                "House RENT",
                Instant.now()
        ));

        transactions.add(new Transaction(
                "TXN-006",
                "ACC-1002",
                new BigDecimal("1500.75"),
                TransactionType.CREDIT,
                "Cashback Reward",
                Instant.now()
        ));

        transactions.add(new Transaction(
                "TXN-007",
                "ACC-1001",
                new BigDecimal("-499.99"),
                TransactionType.DEBIT,
                "Streaming Subscription",
                Instant.now()
        ));

        transactions.add(new Transaction(
                "TXN-008",
                "ACC-1002",
                new BigDecimal("-3000.00"),
                TransactionType.TRANSFER,
                "Transfer to SAVINGS account",
                Instant.now()
        ));

        transactions.add(new Transaction(
                "TXN-009",
                "ACC-1001",
                new BigDecimal("850.00"),
                TransactionType.REFUND,
                "Refund for Online Purchase",
                Instant.now()
        ));

        transactions.add(new Transaction(
                "TXN-009", // intentional duplicate ID
                "ACC-1001",
                new BigDecimal("850.00"),
                TransactionType.REFUND,
                "REFUND for online purchase",
                Instant.now()
        ));

        return transactions;
    }
}