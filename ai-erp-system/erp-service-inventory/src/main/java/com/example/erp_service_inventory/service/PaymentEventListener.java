package com.example.erp_service_inventory.service;

import com.example.erp_service_inventory.config.TenantContext;
import com.example.erp_service_inventory.dto.PaymentEvent;
import com.example.erp_service_inventory.entity.Order;
import com.example.erp_service_inventory.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PaymentEventListener {

    @Autowired
    private OrderRepository orderRepository;

    @KafkaListener(topics = "finance-events", groupId = "inventory-group")
//    @Transactional
    public void handlePaymentEvent(PaymentEvent event) {
        System.out.println("🚨 INVENTORY SERVICE HEARD FINANCE: Order ID " + event.getOrderId() + " is " + event.getPaymentStatus());

        try {
            // 1. Put the Tenant ID in the backpack!
            TenantContext.setTenantId(event.getTenantId());

            // 2. Find the Order in the database
            Optional<Order> optionalOrder = orderRepository.findById(event.getOrderId());

            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();

                // 3. Update the status and save!
                order.setStatus("CONFIRMED");
                orderRepository.save(order);

                System.out.println("✅ INVENTORY SERVICE: Successfully updated Order " + order.getId() + " to CONFIRMED for Tenant: " + event.getTenantId());
            } else {
                System.out.println("❌ INVENTORY SERVICE ERROR: Could not find Order " + event.getOrderId());
            }

        } catch (Exception e) {
            System.out.println("❌ INVENTORY SERVICE ERROR: " + e.getMessage());
        } finally {
            // 4. Always clear the backpack
            TenantContext.clear();
        }
    }
}