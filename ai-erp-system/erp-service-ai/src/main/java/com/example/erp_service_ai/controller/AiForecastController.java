package com.example.erp_service_ai.controller;

import com.example.erp_service_ai.client.InventoryClient;
import com.example.erp_service_ai.service.AiForecastService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/forecast")
public class AiForecastController {

    private final AiForecastService aiForecastService;
    private final InventoryClient inventoryClient; // Inject Feign Client

    public AiForecastController(AiForecastService aiForecastService, InventoryClient inventoryClient) {
        this.aiForecastService = aiForecastService;
        this.inventoryClient = inventoryClient;
    }

    @PostMapping("/demand/{productId}")
    public ResponseEntity<String> predictDemand(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Tenant-ID") String tenantId) {

        // 1. Fetch data from Inventory Service using Feign!
        System.out.println("📡 Fetching sales data from Inventory Service...");
        List<Integer> salesHistory = inventoryClient.getSalesHistory(productId, token, tenantId);

        // 2. Pass the fetched data to the AI
        String prediction = aiForecastService.predictDemand("Product " + productId, salesHistory.toString());

        return ResponseEntity.ok(prediction);
    }
}