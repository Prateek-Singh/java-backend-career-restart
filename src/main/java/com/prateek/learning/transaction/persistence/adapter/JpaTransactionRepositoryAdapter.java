package com.prateek.learning.transaction.persistence.adapter;

import com.prateek.learning.common.exception.DuplicateTransactionException;
import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.persistence.entity.TransactionEntity;
import com.prateek.learning.transaction.persistence.mapper.TransactionEntityMapper;
import com.prateek.learning.transaction.persistence.repository.SpringDataTransactionRepository;
import com.prateek.learning.transaction.repository.TransactionRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
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

        try {
            TransactionEntity savedEntity =
                    springDataTransactionRepository.saveAndFlush(transactionEntity);

            return mapper.toDomain(savedEntity);

        } catch (DataIntegrityViolationException exception) {
            if (isDuplicateTransactionIdViolation(exception)) {
                throw new DuplicateTransactionException(
                        "Transaction with id "
                                + transaction.getId()
                                + " already exists"
                );
            }

            throw exception;
        }
    }

    private boolean isDuplicateTransactionIdViolation(
            DataIntegrityViolationException exception
    ) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintException) {

                String constraintName = constraintException.getConstraintName();

                if (constraintName != null
                        && constraintName.toLowerCase(Locale.ROOT)
                        .contains("uk_transactions_transaction_id")) {
                    return true;
                }
            }

            cause = cause.getCause();
        }

        return false;
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
