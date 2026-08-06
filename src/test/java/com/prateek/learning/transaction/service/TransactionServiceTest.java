package com.prateek.learning.transaction.service;

import com.prateek.learning.transaction.dto.CreateTransactionRequest;
import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void shouldSaveAndReturnTransactionWhenValid() {

        BigDecimal amount = new BigDecimal("25.00");

        CreateTransactionRequest request = new CreateTransactionRequest(
                "Txn-123",
                "ACC-111",
                amount,
                "CREDIT",
                "Monthly EMI"
        );

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction savedTxn = transactionService.createTransaction(request);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        verify(transactionRepository).save(captor.capture());

        Transaction transaction = captor.getValue();

        assertEquals(request.id(), transaction.getId());
        assertEquals(request.accountId(), transaction.getAccountId());
        assertEquals(request.amount(), transaction.getAmount());
        assertEquals(request.type(), transaction.getType());
        assertEquals(request.description(), transaction.getDescription());
        assertNotNull(transaction.getTimestamp());
        assertSame(savedTxn, transaction);
    }
}