package com.prateek.learning.day04.java.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prateek.learning.day01.java.Transaction;
import com.prateek.learning.day01.java.TransactionService;
import com.prateek.learning.day03.java.exceptions.InvalidTransactionAmountException;
import com.prateek.learning.day03.java.exceptions.TransactionNotFoundException;
import com.prateek.learning.day03.java.spring.exceptionhandling.GlobalExceptionHandler;
import com.prateek.learning.day04.java.springboot.dto.CreateTransactionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(GlobalExceptionHandler.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnTransactionWhenTransactionExists() throws Exception {
        Transaction transaction = new Transaction(
                "TXN-001",
                "ACC-1001",
                new BigDecimal("25000.00"),
                "CREDIT",
                "Monthly Salary",
                LocalDateTime.of(2026, 8, 1, 9, 30)
        );

        when(transactionService.findById("TXN-001"))
                .thenReturn(transaction);

        mockMvc.perform(get("/transactions/TXN-001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.id").value("TXN-001"))
                .andExpect(jsonPath("$.accountId").value("ACC-1001"))
                .andExpect(jsonPath("$.amount").value(25000.00))
                .andExpect(jsonPath("$.type").value("CREDIT"))
                .andExpect(jsonPath("$.description").value("Monthly Salary"));
    }

    @Test
    void shouldReturnNotFoundWhenTransactionDoesNotExist() throws Exception {
        when(transactionService.findById("TXN-999"))
                .thenThrow(
                        new TransactionNotFoundException(
                                "Transaction not found for ID: TXN-999"
                        )
                );

        mockMvc.perform(get("/transactions/TXN-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(
                        jsonPath("$.message")
                                .value("Transaction not found for ID: TXN-999")
                );
    }

    @Test
    void shouldReturnTransactionsForExistingAccount() throws Exception {
        List<Transaction> transactions = List.of(
                new Transaction(
                        "TXN-001",
                        "ACC-1001",
                        new BigDecimal("25000.00"),
                        "CREDIT",
                        "Monthly Salary",
                        LocalDateTime.of(2026, 8, 1, 9, 30)
                ),
                new Transaction(
                        "TXN-002",
                        "ACC-1001",
                        new BigDecimal("-1250.50"),
                        "DEBIT",
                        "Electricity Bill Payment",
                        LocalDateTime.of(2026, 8, 1, 11, 15)
                )
        );

        when(transactionService.findByAccountId("ACC-1001"))
                .thenReturn(transactions);

        mockMvc.perform(get("/transactions/account/ACC-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("TXN-001"))
                .andExpect(jsonPath("$[0].accountId").value("ACC-1001"))
                .andExpect(jsonPath("$[1].id").value("TXN-002"))
                .andExpect(jsonPath("$[1].accountId").value("ACC-1001"));
    }

    @Test
    void shouldReturnEmptyListForUnknownAccount() throws Exception {
        when(transactionService.findByAccountId("UNKNOWN"))
                .thenReturn(List.of());

        mockMvc.perform(get("/transactions/account/UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnCreatedTransactionForValidRequest() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TXN-123",
                "ACC-1111",
                new BigDecimal("35000.00"),
                "VISA",
                "Monthly EMI"
        );

        Transaction createdTransaction = new Transaction(
                "TXN-123",
                "ACC-1111",
                new BigDecimal("35000.00"),
                "VISA",
                "Monthly EMI",
                LocalDateTime.of(2026, 8, 4, 10, 30)
        );

        when(transactionService.createTransaction(request))
                .thenReturn(createdTransaction);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("TXN-123"))
                .andExpect(jsonPath("$.accountId").value("ACC-1111"))
                .andExpect(jsonPath("$.amount").value(35000.00))
                .andExpect(jsonPath("$.type").value("VISA"))
                .andExpect(jsonPath("$.description").value("Monthly EMI"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnBadRequestWhenTransactionIdIsBlank() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                " ",
                "ACC-1111",
                new BigDecimal("35000.00"),
                "VISA",
                "Monthly EMI"
        );

        when(transactionService.createTransaction(request))
                .thenThrow(
                        new IllegalArgumentException(
                                "Transaction id is required"
                        )
                );

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Transaction id is required"));
    }

    @Test
    void shouldReturnBadRequestWhenTransactionAmountIsZero() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TXN-124",
                "ACC-1111",
                BigDecimal.ZERO,
                "VISA",
                "Monthly EMI"
        );

        when(transactionService.createTransaction(request))
                .thenThrow(
                        new InvalidTransactionAmountException(
                                "Transaction amount must be greater than zero"
                        )
                );

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Transaction amount must be greater than zero"));
    }
}