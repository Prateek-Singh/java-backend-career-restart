package com.prateek.learning.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prateek.learning.transaction.dto.CreateTransactionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

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

    @Test
    void shouldCreateAndRetrieveTransactionById() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TXN-111",
                "ACC-111",
                BigDecimal.TEN,
                "CREDIT",
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
}