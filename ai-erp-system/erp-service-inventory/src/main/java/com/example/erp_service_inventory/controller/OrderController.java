package com.example.erp_service_inventory.controller;

import com.example.erp_service_inventory.entity.Order;
import com.example.erp_service_inventory.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PreAuthorize("hasAuthority('ROLE_INVENTORY_ADMIN')")
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        Order savedOrder = orderService.placeOrder(order);
        return ResponseEntity.ok(savedOrder);
    }
}