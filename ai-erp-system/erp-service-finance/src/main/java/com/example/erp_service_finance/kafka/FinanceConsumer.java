package com.example.erp_service_finance.kafka;

import com.example.erp_service_finance.dto.OrderEvent;
import com.example.erp_service_finance.dto.PaymentEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class FinanceConsumer {

    private final FinanceProducer financeProducer;

    // 🌟 Inject the Producer via constructor
    public FinanceConsumer(FinanceProducer financeProducer) {
        this.financeProducer = financeProducer;
    }

    @KafkaListener(topics = "order-events", groupId = "finance-group")
    public void consumeOrderEvent(OrderEvent event) {
        System.out.println("\n==========================================");
        System.out.println("🏦 FINANCE SAGA: Message Received!");
        System.out.println("Order ID: " + event.getOrderId() + " | Quantity: " + event.getQuantity());

        // 1. Calculate Cost
        double totalCost = event.getQuantity() * 200.00;
        System.out.println("Attempting to deduct $" + totalCost + " from the ledger...");

        // 2. 🚨 THE BUSINESS RULE (Compensating Logic)
        String finalStatus;
        if (totalCost > 5000.00) {
            System.out.println("❌ DECLINED: Insufficient corporate funds for Tata Motors!");
            finalStatus = "REJECTED";
        } else {
            System.out.println("✅ APPROVED: Payment Processed Successfully!");
            finalStatus = "APPROVED";
        }

        // 3. 🚀 THE RETURN TRIP: Tell Inventory the result
        PaymentEvent paymentResponse = new PaymentEvent(
                event.getTenantId(),
                event.getOrderId(),
                finalStatus // This will now send either APPROVED or REJECTED
        );
        financeProducer.publishPaymentEvent(paymentResponse);

        System.out.println("==========================================\n");
    }
}