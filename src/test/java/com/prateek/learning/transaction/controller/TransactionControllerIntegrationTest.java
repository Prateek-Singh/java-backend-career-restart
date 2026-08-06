package com.prateek.learning.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prateek.learning.transaction.dto.CreateTransactionRequest;
import com.prateek.learning.transaction.model.TransactionType;
import com.prateek.learning.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.clear();
    }

    @Test
    void shouldCreateAndRetrieveTransactionById() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TXN-111",
                "ACC-111",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Monthly EMI"
        );

        mockMvc.perform(
                        post("/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("TXN-111"))
                .andExpect(jsonPath("$.accountId").value("ACC-111"))
                .andExpect(jsonPath("$.amount").value(10))
                .andExpect(jsonPath("$.type").value("CREDIT"))
                .andExpect(jsonPath("$.description").value("Monthly EMI"))
                .andExpect(jsonPath("$.timestamp").exists());

        mockMvc.perform(
                        get("/transactions/TXN-111")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("TXN-111"))
                .andExpect(jsonPath("$.accountId").value("ACC-111"))
                .andExpect(jsonPath("$.amount").value(10))
                .andExpect(jsonPath("$.type").value("CREDIT"))
                .andExpect(jsonPath("$.description").value("Monthly EMI"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldCreateAndRetrieveTransactionByAccountId() throws Exception {
        CreateTransactionRequest transactionRequest1 = new CreateTransactionRequest(
                "TXN-113",
                "ACC-111",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Monthly EMI"
        );

        CreateTransactionRequest transactionRequest2 = new CreateTransactionRequest(
                "TXN-114",
                "ACC-111",
                new BigDecimal("25"),
                TransactionType.DEBIT,
                "Monthly Salary"
        );

        mockMvc.perform(
                        post("/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(transactionRequest1))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("TXN-113"))
                .andExpect(jsonPath("$.accountId").value("ACC-111"))
                .andExpect(jsonPath("$.amount").value(10))
                .andExpect(jsonPath("$.type").value("CREDIT"))
                .andExpect(jsonPath("$.description").value("Monthly EMI"))
                .andExpect(jsonPath("$.timestamp").exists());

        mockMvc.perform(
                        post("/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(transactionRequest2))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("TXN-114"))
                .andExpect(jsonPath("$.accountId").value("ACC-111"))
                .andExpect(jsonPath("$.amount").value(25))
                .andExpect(jsonPath("$.type").value("DEBIT"))
                .andExpect(jsonPath("$.description").value("Monthly Salary"))
                .andExpect(jsonPath("$.timestamp").exists());

        mockMvc.perform(get("/transactions/account/{accountId}", "ACC-111"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].id",
                        containsInAnyOrder("TXN-113", "TXN-114")))
                .andExpect(jsonPath("$[*].accountId",
                        everyItem(is("ACC-111"))));
    }
}