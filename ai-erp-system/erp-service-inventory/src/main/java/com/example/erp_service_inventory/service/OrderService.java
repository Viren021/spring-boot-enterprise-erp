package com.example.erp_service_inventory.service;

import com.example.erp_service_inventory.config.TenantContext;
import com.example.erp_service_inventory.dto.OrderEvent;
import com.example.erp_service_inventory.entity.Order;
import com.example.erp_service_inventory.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public Order placeOrder(Order orderRequest) {
        // 1. Set initial status and Save to DB
        orderRequest.setStatus("PENDING");
        Order savedOrder = orderRepository.save(orderRequest);

        // 2. Create the Event Message
        OrderEvent event = new OrderEvent(
                TenantContext.getTenantId(), // Grab the tenant from the backpack!
                savedOrder.getId(),
                savedOrder.getProductId(),
                savedOrder.getQuantity(),
                savedOrder.getStatus()
        );

        // 3. Publish to Kafka!
        // We send it to a topic named "order-events"
        kafkaTemplate.send("order-events", event);

        System.out.println("Order saved and event published to Kafka for tenant: " + event.getTenantId());

        return savedOrder;
    }
}