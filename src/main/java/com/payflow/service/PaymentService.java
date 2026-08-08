package com.payflow.service;

import com.payflow.event.PaymentCompletedEvent;
import com.payflow.event.PaymentEventPublisher;
import com.payflow.model.*;
import com.payflow.model.dto.PaymentRequest;
import com.payflow.model.dto.PaymentResponse;
import com.payflow.repository.AccountRepository;
import com.payflow.repository.LedgerEntryRepository;
import com.payflow.repository.TransactionRepository;
import com.payflow.service.risk.RiskEngineService;
import com.payflow.service.risk.RiskEvaluationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class PaymentService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final RiskEngineService riskEngineService;
    private final PaymentEventPublisher eventPublisher;

    public PaymentService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          LedgerEntryRepository ledgerEntryRepository,
                          RiskEngineService riskEngineService,
                          PaymentEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.riskEngineService = riskEngineService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request, String idempotencyKey) {
        // 1. Idempotency Check
        Optional<Transaction> existingTransaction = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTransaction.isPresent()) {
            log.info("Idempotent request detected. Returning existing transaction for key: {}", idempotencyKey);
            Transaction tx = existingTransaction.get();
            return PaymentResponse.builder()
                    .transactionId(tx.getId())
                    .idempotencyKey(tx.getIdempotencyKey())
                    .status(tx.getStatus())
                    .amount(tx.getAmount())
                    .message("Returned existing transaction due to idempotency key match")
                    .build();
        }

        // 2. Fetch Accounts
        Account sourceAccount = accountRepository.findByAccountNumber(request.getSourceAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        Account targetAccount = accountRepository.findByAccountNumber(request.getTargetAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Target account not found"));

        // 3. Create initial Transaction record (PENDING)
        Transaction transaction = Transaction.builder()
                .idempotencyKey(idempotencyKey)
                .amount(request.getAmount())
                .sourceAccount(sourceAccount)
                .targetAccount(targetAccount)
                .status(TransactionStatus.PENDING)
                .build();
        transaction = transactionRepository.save(transaction);

        try {
            // 4. Risk Engine Evaluation
            riskEngineService.evaluateTransaction(sourceAccount, targetAccount, request.getAmount());
            transaction.setStatus(TransactionStatus.AUTHORIZED);

            // 5. Update Balances
            sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
            targetAccount.setBalance(targetAccount.getBalance().add(request.getAmount()));
            accountRepository.save(sourceAccount);
            accountRepository.save(targetAccount);

            // 6. Create Ledger Entries
            LedgerEntry debitEntry = LedgerEntry.builder()
                    .transaction(transaction)
                    .account(sourceAccount)
                    .amount(request.getAmount())
                    .type(LedgerEntryType.DEBIT)
                    .build();
            LedgerEntry creditEntry = LedgerEntry.builder()
                    .transaction(transaction)
                    .account(targetAccount)
                    .amount(request.getAmount())
                    .type(LedgerEntryType.CREDIT)
                    .build();
            ledgerEntryRepository.save(debitEntry);
            ledgerEntryRepository.save(creditEntry);

            // 7. Mark as COMPLETED
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);

            // 8. Publish async event (after tx commit in a real-world scenario with TransactionSynchronizationManager, 
            // but for simplicity we publish here. RabbitMQ handles the message decoupled from HTTP)
            PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                    .transactionId(transaction.getId())
                    .idempotencyKey(transaction.getIdempotencyKey())
                    .amount(transaction.getAmount())
                    .sourceAccountId(sourceAccount.getId())
                    .targetAccountId(targetAccount.getId())
                    .timestamp(LocalDateTime.now().toString())
                    .build();
            eventPublisher.publishPaymentCompletedEvent(event);

            return PaymentResponse.builder()
                    .transactionId(transaction.getId())
                    .idempotencyKey(transaction.getIdempotencyKey())
                    .status(TransactionStatus.COMPLETED)
                    .amount(transaction.getAmount())
                    .message("Payment processed successfully")
                    .build();

        } catch (RiskEvaluationException e) {
            log.warn("Risk evaluation failed for transaction {}: {}", transaction.getId(), e.getMessage());
            transaction.setStatus(TransactionStatus.REJECTED);
            transaction.setFailureReason(e.getMessage());
            transactionRepository.save(transaction);
            return PaymentResponse.builder()
                    .transactionId(transaction.getId())
                    .idempotencyKey(transaction.getIdempotencyKey())
                    .status(TransactionStatus.REJECTED)
                    .amount(transaction.getAmount())
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Payment processing failed for transaction {}", transaction.getId(), e);
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Internal system error");
            transactionRepository.save(transaction);
            return PaymentResponse.builder()
                    .transactionId(transaction.getId())
                    .idempotencyKey(transaction.getIdempotencyKey())
                    .status(TransactionStatus.FAILED)
                    .amount(transaction.getAmount())
                    .message("Payment processing failed")
                    .build();
        }
    }
}
