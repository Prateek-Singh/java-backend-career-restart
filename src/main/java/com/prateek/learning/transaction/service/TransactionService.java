package com.prateek.learning.transaction.service;

import com.prateek.learning.transaction.dto.CreateTransactionRequest;
import com.prateek.learning.transaction.exception.InvalidTransactionAmountException;
import com.prateek.learning.transaction.exception.TransactionNotFoundException;
import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.model.TransactionType;
import com.prateek.learning.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
    
    public List<Transaction> findByAccountId(String accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    private void validateAmount(BigDecimal amount) {
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

    public Transaction createTransaction(CreateTransactionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Transaction request is required"
            );
        }

        validateTransactionId(request.id());
        validateAccountId(request.accountId());
        validateAmount(request.amount());
        validateTransactionType(request.type());

        Transaction transaction = new Transaction();
        transaction.setId(request.id());
        transaction.setAccountId(request.accountId());
        transaction.setAmount(request.amount());
        transaction.setType(request.type());
        transaction.setDescription(request.description());
        transaction.setTimestamp(Instant.now());
        return transactionRepository.save(transaction);
    }

    public Transaction getTransactionById(String transactionId) {
        validateTransactionId(transactionId);

        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }

    private void validateTransactionId(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("Transaction id is required");
        }
    }

    private void validateAccountId(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account id is required");
        }
    }

    private void validateTransactionType(TransactionType type) {
        if (type == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }
    }
}
