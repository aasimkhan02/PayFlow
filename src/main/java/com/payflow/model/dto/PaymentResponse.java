package com.payflow.model.dto;

import com.payflow.model.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentResponse {
    private Long transactionId;
    private String idempotencyKey;
    private TransactionStatus status;
    private BigDecimal amount;
    private String message;
}
