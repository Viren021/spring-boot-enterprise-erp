package com.example.erp_service_inventory.controller;

import com.example.erp_service_inventory.dto.OrderEvent;
import com.example.erp_service_inventory.entity.Order; // 🌟 Ensure this matches your package!
import com.example.erp_service_inventory.repository.OrderRepository; // 🌟 Ensure this matches your package!
import com.example.erp_service_inventory.kafka.InventoryProducer;
import com.example.erp_service_inventory.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/stock")
@CrossOrigin(origins = "http://localhost:4200") // 🌟 Allows Angular to call these APIs!
public class ProductController {

    private final ProductService productService;
    private final InventoryProducer inventoryProducer;
    private final OrderRepository orderRepository; // 🌟 NEW: The DB Repository

    // 🌟 Constructor Injection
    @Autowired
    public ProductController(ProductService productService,
                             InventoryProducer inventoryProducer,
                             OrderRepository orderRepository) { // 🌟 NEW: Injected here
        this.productService = productService;
        this.inventoryProducer = inventoryProducer;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Integer> checkStock(@PathVariable Long productId) {
        Integer stock = productService.getStockLevel(productId);
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/{productId}/sales-history")
    public ResponseEntity<List<Integer>> getSalesHistory(@PathVariable Long productId) {
        // In a real scenario, this would run a complex SQL GROUP BY query on the Orders table
        List<Integer> pastSixMonthsSales = List.of(100, 105, 115, 130, 150, 180);
        return ResponseEntity.ok(pastSixMonthsSales);
    }

    // ==========================================
    // 🚀 THE DISTRIBUTED SAGA TRIGGER
    // ==========================================

    // 🔒 ONLY Inventory Admin can trigger the Saga!
    @PreAuthorize("hasAuthority('ROLE_INVENTORY_ADMIN')")
    @PostMapping("/trigger-saga")
    public ResponseEntity<String> triggerOrderSaga() {

        // 1. Create the order but DO NOT set the ID manually!
        Order newDbOrder = new Order();
        // REMOVED the setId() line so PostgreSQL can auto-generate it!
        newDbOrder.setProductId(1L);
        newDbOrder.setQuantity(500);
        newDbOrder.setStatus("PENDING");

        // 2. 💾 FORCE SAVE TO DATABASE FIRST!
        // saveAndFlush forces Postgres to write immediately.
        // We capture the "savedOrder" which now contains the REAL database ID.
        Order savedOrder = orderRepository.saveAndFlush(newDbOrder);

        // 3. 📨 CREATE THE EVENT
        // We use the exact ID from the saved object so there is zero mismatch!
        OrderEvent newOrder = new OrderEvent(
                "tata_motors",
                savedOrder.getId(),           // 🌟 The REAL Postgres ID!
                savedOrder.getProductId(),
                savedOrder.getQuantity(),
                savedOrder.getStatus()
        );

        // 4. 🚀 PUBLISH TO KAFKA
        inventoryProducer.publishOrderEvent(newOrder);

        return ResponseEntity.ok("✅ Saga Initiated! Order " + newOrder.getOrderId() + " saved to DB and published to Kafka.");
    }
}