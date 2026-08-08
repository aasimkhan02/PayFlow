package com.payflow.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {
    private Long transactionId;
    private String idempotencyKey;
    private BigDecimal amount;
    private Long sourceAccountId;
    private Long targetAccountId;
    private String timestamp;
}
