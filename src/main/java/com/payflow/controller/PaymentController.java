package com.payflow.controller;

import com.payflow.model.dto.PaymentRequest;
import com.payflow.model.dto.PaymentResponse;
import com.payflow.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    PaymentResponse.builder().message("Idempotency-Key header is required").build()
            );
        }

        PaymentResponse response = paymentService.processPayment(request, idempotencyKey);
        
        if ("REJECTED".equals(response.getStatus().name()) || "FAILED".equals(response.getStatus().name())) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        }

        return ResponseEntity.ok(response);
    }
}
