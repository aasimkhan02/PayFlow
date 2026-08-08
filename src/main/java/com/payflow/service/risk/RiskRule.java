package com.payflow.service.risk;

import com.payflow.model.Account;
import java.math.BigDecimal;

public interface RiskRule {
    void evaluate(Account sourceAccount, Account targetAccount, BigDecimal amount);
}
