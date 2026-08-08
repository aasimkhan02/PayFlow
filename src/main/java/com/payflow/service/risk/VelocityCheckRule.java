package com.payflow.service.risk;

import com.payflow.model.Account;
import com.payflow.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class VelocityCheckRule implements RiskRule {

    private final TransactionRepository transactionRepository;

    @Value("${payflow.risk.velocity.max-transactions:5}")
    private int maxTransactions;

    @Value("${payflow.risk.velocity.time-window-seconds:60}")
    private int timeWindowSeconds;

    public VelocityCheckRule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void evaluate(Account sourceAccount, Account targetAccount, BigDecimal amount) {
        LocalDateTime windowStart = LocalDateTime.now().minusSeconds(timeWindowSeconds);
        long recentTransactionsCount = transactionRepository.countBySourceAccountIdAndCreatedAtAfter(sourceAccount.getId(), windowStart);

        if (recentTransactionsCount >= maxTransactions) {
            throw new RiskEvaluationException("Transaction velocity exceeded. Maximum " + maxTransactions + " transactions allowed per " + timeWindowSeconds + " seconds.");
        }
    }
}
