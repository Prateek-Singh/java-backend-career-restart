package com.prateek.learning.java.day01;

import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.repository.InMemoryTransactionRepository;
import com.prateek.learning.transaction.repository.TransactionRepository;
import com.prateek.learning.transaction.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        TransactionRepository repository = new InMemoryTransactionRepository();
        TransactionService transactionService = new TransactionService(repository);

        List<Transaction> transactions = createSampleTransactions();
        List<Transaction> emptyTransactions = List.of();

        BigDecimal calculateTotalAmount = transactionService.calculateTotalAmount(transactions);
        System.out.println("Total amount: " + calculateTotalAmount);

        List<Transaction> transaction = transactionService.findByAccountId(transactions, "ACC-1001");
        System.out.println("Transaction Id: " + transaction.get(0).getId() + " Id : " + transaction.get(1).getId());
    }

    private static List<Transaction> createSampleTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        transactions.add(new Transaction(
                "TXN-001",
                "ACC-1001",
                new BigDecimal("25000.00"),
                "CREDIT",
                "Monthly Salary",
                LocalDateTime.of(2026, 8, 1, 9, 30)
        ));

        transactions.add(new Transaction(
                "TXN-002",
                "ACC-1001",
                new BigDecimal("-1250.50"),
                "DEBIT",
                "Electricity Bill Payment",
                LocalDateTime.of(2026, 8, 1, 11, 15)
        ));

        transactions.add(new Transaction(
                "TXN-003",
                "ACC-1002",
                new BigDecimal("5000.00"),
                "CREDIT",
                "Freelance Project PAYMENT",
                LocalDateTime.of(2026, 8, 1, 12, 0)
        ));

        transactions.add(new Transaction(
                "TXN-004",
                "ACC-1002",
                new BigDecimal("-750.25"),
                "DEBIT",
                "Online Grocery purchase",
                LocalDateTime.of(2026, 8, 1, 14, 10)
        ));

        transactions.add(new Transaction(
                "TXN-005",
                "ACC-1001",
                new BigDecimal("-2200.00"),
                "DEBIT",
                "House RENT",
                LocalDateTime.of(2026, 8, 2, 8, 45)
        ));

        transactions.add(new Transaction(
                "TXN-006",
                "ACC-1002",
                new BigDecimal("1500.75"),
                "CREDIT",
                "Cashback Reward",
                LocalDateTime.of(2026, 8, 2, 10, 20)
        ));

        transactions.add(new Transaction(
                "TXN-007",
                "ACC-1001",
                new BigDecimal("-499.99"),
                "DEBIT",
                "Streaming Subscription",
                LocalDateTime.of(2026, 8, 2, 13, 5)
        ));

        transactions.add(new Transaction(
                "TXN-008",
                "ACC-1002",
                new BigDecimal("-3000.00"),
                "TRANSFER",
                "Transfer to SAVINGS account",
                LocalDateTime.of(2026, 8, 2, 15, 30)
        ));

        transactions.add(new Transaction(
                "TXN-009",
                "ACC-1001",
                new BigDecimal("850.00"),
                "REFUND",
                "Refund for Online Purchase",
                LocalDateTime.of(2026, 8, 2, 17, 40)
        ));

        transactions.add(new Transaction(
                "TXN-009", // intentional duplicate ID
                "ACC-1001",
                new BigDecimal("850.00"),
                "REFUND",
                "REFUND for online purchase",
                LocalDateTime.of(2026, 8, 2, 17, 40)
        ));

        return transactions;
    }
}