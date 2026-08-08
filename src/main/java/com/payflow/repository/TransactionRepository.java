package com.payflow.repository;

import com.payflow.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    long countBySourceAccountIdAndCreatedAtAfter(Long sourceAccountId, LocalDateTime after);
}
