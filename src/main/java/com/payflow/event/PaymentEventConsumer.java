package com.payflow.event;

import com.payflow.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentEventConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("Received payment completed event! Transaction ID: {}, Amount: {}, Source Account: {}",
                event.getTransactionId(), event.getAmount(), event.getSourceAccountId());
        
        // In a real application, you might send an email receipt, update a reporting database, etc.
        log.info("Successfully processed post-payment operations for transaction {}", event.getTransactionId());
    }
}
