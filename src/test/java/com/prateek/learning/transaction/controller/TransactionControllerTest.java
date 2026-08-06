package com.prateek.learning.transaction.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prateek.learning.common.exception.GlobalExceptionHandler;
import com.prateek.learning.transaction.dto.CreateTransactionRequest;
import com.prateek.learning.transaction.exception.TransactionNotFoundException;
import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.model.TransactionType;
import com.prateek.learning.transaction.service.TransactionService;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
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
                TransactionType.CREDIT,
                "Monthly Salary",
                LocalDateTime.of(2026, 8, 1, 9, 30)
        );

        when(transactionService.getTransactionById("TXN-001"))
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
        when(transactionService.getTransactionById("TXN-999"))
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
                        TransactionType.CREDIT,
                        "Monthly Salary",
                        LocalDateTime.of(2026, 8, 1, 9, 30)
                ),
                new Transaction(
                        "TXN-002",
                        "ACC-1001",
                        new BigDecimal("-1250.50"),
                        TransactionType.DEBIT,
                        "Electricity Bill Payment",
                        LocalDateTime.of(2026, 8, 1, 11, 15)
                )
        );

        when(transactionService.findByAccountId("ACC-1001"))
                .thenReturn(transactions);

        mockMvc.perform(get("/transactions/account/ACC-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath(
                        "$[*].id",
                        containsInAnyOrder("TXN-001", "TXN-002")
                ))
                .andExpect(jsonPath(
                        "$[*].accountId",
                        everyItem(is("ACC-1001"))
                ));
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
                TransactionType.TRANSFER,
                "Monthly EMI"
        );

        Transaction createdTransaction = new Transaction(
                "TXN-123",
                "ACC-1111",
                new BigDecimal("35000.00"),
                TransactionType.TRANSFER,
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
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.description").value("Monthly EMI"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnBadRequestWhenTransactionIdIsBlank() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                " ",
                "ACC-1111",
                new BigDecimal("35000.00"),
                TransactionType.REFUND,
                "Monthly EMI"
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
                        .value("id cannot be null or blank"));

        verifyNoInteractions(transactionService);
    }

    @Test
    void shouldReturnBadRequestWhenTransactionAmountIsZero() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TXN-124",
                "ACC-1111",
                BigDecimal.ZERO,
                TransactionType.REFUND,
                "Monthly EMI"
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
                        .value("amount must be greater than zero"));

        verifyNoInteractions(transactionService);
    }

    @Test
    void shouldReturnBadRequestWhenAccountIdIsBlank() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TXN-123",
                " ",
                new BigDecimal("35000.00"),
                TransactionType.REFUND,
                "Monthly EMI"
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
                        .value("accountId cannot be null or blank"));

        verifyNoInteractions(transactionService);
    }

    @Test
    void shouldReturnBadRequestWhenAmountIsNull() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TXN-123",
                "ACC-1111",
                null,
                TransactionType.REFUND,
                "Monthly EMI"
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
                        .value("amount cannot be null"));

        verifyNoInteractions(transactionService);
    }

    @Test
    void shouldReturnBadRequestWhenTypeIsNull() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TXN-123",
                "ACC-1111",
                BigDecimal.TEN,
                null,
                "Monthly EMI"
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
                        .value("type cannot be null"));

        verifyNoInteractions(transactionService);
    }

    @Test
    void shouldReturnBadRequestWhenDescriptionIsBlank() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TXN-123",
                "ACC-1111",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                " "
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
                        .value("description cannot be null or blank"));

        verifyNoInteractions(transactionService);
    }

    @Test
    void shouldReturnBadRequestWhenTypeIsInvalid() throws Exception {
        String json = """
        {
          "id": "TXN-123",
          "accountId": "ACC-1111",
          "amount": 10,
          "type": "SALARY",
          "description": "Monthly salary"
        }
        """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid request body"));

        verifyNoInteractions(transactionService);
    }

}