package com.prateek.learning.day04.java.springboot;

import com.prateek.learning.day01.java.Transaction;
import com.prateek.learning.day01.java.TransactionService;
import com.prateek.learning.day04.java.springboot.dto.CreateTransactionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransactionById(
            @PathVariable("transactionId") String transactionId) {
        Transaction transaction = transactionService.findById(transactionId);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>> getTransactionsByAccountId(
            @PathVariable("accountId") String accountId
    ) {
        return ResponseEntity.ok(
                transactionService.findByAccountId(accountId)
        );
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
            @RequestBody CreateTransactionRequest request
    ) {
        Transaction transaction =
                transactionService.createTransaction(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transaction);
    }
}
