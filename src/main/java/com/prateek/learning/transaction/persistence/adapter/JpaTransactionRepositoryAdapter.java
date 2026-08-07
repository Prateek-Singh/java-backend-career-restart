package com.prateek.learning.transaction.persistence.adapter;

import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.persistence.entity.TransactionEntity;
import com.prateek.learning.transaction.persistence.mapper.TransactionEntityMapper;
import com.prateek.learning.transaction.persistence.repository.SpringDataTransactionRepository;
import com.prateek.learning.transaction.repository.TransactionRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Profile("jpa")
public class JpaTransactionRepositoryAdapter
        implements TransactionRepository {

    private final SpringDataTransactionRepository springDataTransactionRepository;
    private final TransactionEntityMapper mapper;

    public JpaTransactionRepositoryAdapter(
            SpringDataTransactionRepository springDataTransactionRepository,
            TransactionEntityMapper mapper) {
        this.springDataTransactionRepository = springDataTransactionRepository;
        this.mapper = mapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity transactionEntity = mapper.toEntity(transaction);
        TransactionEntity savedEntity = springDataTransactionRepository.save(transactionEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Transaction> findById(String transactionId) {
        return springDataTransactionRepository
                .findByTransactionId(transactionId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Transaction> findByAccountId(String accountId) {
        return springDataTransactionRepository
                .findByAccountId(accountId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void clear() {
        springDataTransactionRepository.deleteAll();
    }
}
