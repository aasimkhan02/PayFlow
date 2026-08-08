package com.payflow.service.risk;

import com.payflow.model.Account;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RiskEngineService {

    private final List<RiskRule> riskRules;

    public RiskEngineService(List<RiskRule> riskRules) {
        this.riskRules = riskRules;
    }

    public void evaluateTransaction(Account sourceAccount, Account targetAccount, BigDecimal amount) {
        for (RiskRule rule : riskRules) {
            rule.evaluate(sourceAccount, targetAccount, amount);
        }
    }
}
