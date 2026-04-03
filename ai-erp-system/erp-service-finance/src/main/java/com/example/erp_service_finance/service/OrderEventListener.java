package com.example.erp_service_finance.service;

import com.example.erp_service_finance.config.TenantContext;
import com.example.erp_service_finance.dto.OrderEvent;
import com.example.erp_service_finance.dto.PaymentEvent;
import com.example.erp_service_finance.entity.Ledger;
import com.example.erp_service_finance.repository.LedgerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderEventListener {

    @Autowired
    private LedgerRepository ledgerRepository;

    // ADD THIS: The megaphone to talk back to Inventory!
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order-events", groupId = "finance-group")
//    @Transactional
    public void handleOrderEvent(OrderEvent event) {
        System.out.println("🚨 FINANCE SERVICE RECEIVED EVENT: Order ID " + event.getOrderId());

        try {
            TenantContext.setTenantId(event.getTenantId());

            Double totalCost = event.getQuantity() * 100.0;

            Ledger ledger = new Ledger();
            ledger.setOrderId(event.getOrderId());
            ledger.setAmount(totalCost);
            ledger.setTransactionType("DEBIT");
            ledgerRepository.save(ledger);

            System.out.println("✅ FINANCE SERVICE: Successfully charged $" + totalCost + " for Tenant: " + event.getTenantId());

            // --- NEW SAGA LOGIC: SEND THE REPLY! ---
            PaymentEvent replyEvent = new PaymentEvent(
                    event.getTenantId(),
                    event.getOrderId(),
                    "APPROVED"
            );

            // Send it to a brand new topic called 'finance-events'
            kafkaTemplate.send("finance-events", replyEvent);
            System.out.println("📢 FINANCE SERVICE: Sent APPROVED message back to Kafka!");

        } catch (Exception e) {
            System.out.println("❌ FINANCE SERVICE ERROR: " + e.getMessage());
        } finally {
            TenantContext.clear();
        }
    }
}