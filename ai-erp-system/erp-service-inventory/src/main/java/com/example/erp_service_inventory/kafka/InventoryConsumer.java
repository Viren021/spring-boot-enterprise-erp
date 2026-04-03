package com.example.erp_service_inventory.kafka;

import com.example.erp_service_inventory.dto.PaymentEvent;
import com.example.erp_service_inventory.entity.Order;
import com.example.erp_service_inventory.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InventoryConsumer {

    private final OrderRepository orderRepository;

    // Inject your database repository so we can update the order!
    public InventoryConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "payment-events", groupId = "inventory-group")
    public void consumePaymentEvent(PaymentEvent event) {
        System.out.println("\n==========================================");
        System.out.println("🚨 INVENTORY SAGA: Heard back from Finance!");
        System.out.println("Order ID: " + event.getOrderId() + " is " + event.getPaymentStatus());

        // 1. Find the PENDING order in the database
        Optional<Order> orderOptional = orderRepository.findById(event.getOrderId());

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();

            // 2. Update the status based on what Finance said
            if ("APPROVED".equals(event.getPaymentStatus())) {
                order.setStatus("COMPLETED");
                System.out.println("✅ Order Status updated to COMPLETED. Ready to ship!");
            } else {
                order.setStatus("CANCELLED");
                System.out.println("❌ Payment failed. Order CANCELLED.");
            }

            // 3. Save the updated order back to PostgreSQL
            orderRepository.save(order);

        } else {
            System.out.println("❌ ERROR: Could not find Order " + event.getOrderId() + " in database!");
        }
        System.out.println("==========================================\n");
    }
}