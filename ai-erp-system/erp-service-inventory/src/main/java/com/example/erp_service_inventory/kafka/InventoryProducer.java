package com.example.erp_service_inventory.kafka;

import com.example.erp_service_inventory.dto.OrderEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderEvent(OrderEvent event) {
        System.out.println("📦 INVENTORY SAGA: Publishing Order Event -> Order ID: " + event.getOrderId() + " | Tenant: " + event.getTenantId());

        // Throw the event into the "order-events" topic
        kafkaTemplate.send("order-events", event);
    }
}