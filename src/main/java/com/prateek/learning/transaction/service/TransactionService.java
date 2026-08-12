package com.prateek.learning.transaction.service;

import com.prateek.learning.common.exception.DuplicateTransactionException;
import com.prateek.learning.transaction.dto.CreateTransactionRequest;
import com.prateek.learning.transaction.exception.InvalidTransactionAmountException;
import com.prateek.learning.transaction.exception.TransactionNotFoundException;
import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.model.TransactionType;
import com.prateek.learning.transaction.repository.TransactionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByAccountId(String accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    @Transactional
    public Transaction createTransaction(CreateTransactionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Transaction request is required"
            );
        }

        validateTransactionId(request.id());

        if (transactionRepository.findById(request.id()).isPresent()) {
            throw new DuplicateTransactionException("Transaction with id " + request.id() + " already exists");
        }

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

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "transactions", key = "#transactionId")
    public Transaction getTransactionById(String transactionId) {
        validateTransactionId(transactionId);

        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
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
