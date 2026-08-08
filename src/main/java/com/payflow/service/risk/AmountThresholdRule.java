package com.payflow.service.risk;

import com.payflow.model.Account;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AmountThresholdRule implements RiskRule {

    @Value("${payflow.risk.max-transaction-amount:10000.00}")
    private BigDecimal maxAmount;

    @Override
    public void evaluate(Account sourceAccount, Account targetAccount, BigDecimal amount) {
        if (amount.compareTo(maxAmount) > 0) {
            throw new RiskEvaluationException("Transaction amount exceeds the maximum allowed threshold of " + maxAmount);
        }
    }
}
