package com.payflow.service.risk;

import com.payflow.model.Account;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BalanceCheckRule implements RiskRule {

    @Override
    public void evaluate(Account sourceAccount, Account targetAccount, BigDecimal amount) {
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new RiskEvaluationException("Insufficient funds in the source account.");
        }
        
        if (!"ACTIVE".equals(sourceAccount.getStatus())) {
            throw new RiskEvaluationException("Source account is not active.");
        }
    }
}
