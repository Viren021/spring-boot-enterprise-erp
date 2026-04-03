package com.example.erp_service_finance.kafka;

import com.example.erp_service_finance.dto.PaymentEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class FinanceProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FinanceProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentEvent(PaymentEvent event) {
        System.out.println("💸 FINANCE SAGA: Publishing Payment Event -> Order ID: " + event.getOrderId() + " | Status: " + event.getPaymentStatus());

        // Throw the approval into a new topic named "payment-events"
        kafkaTemplate.send("payment-events", event);
    }
}