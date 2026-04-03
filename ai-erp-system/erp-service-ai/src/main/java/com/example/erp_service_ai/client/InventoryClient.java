package com.example.erp_service_ai.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

// This tells Spring: "When I call these methods, secretly make an HTTP request to port 8083!"
@FeignClient(name = "inventory-service", url = "http://localhost:8083")
public interface InventoryClient {

    @GetMapping("/api/v1/inventory/stock/{productId}/sales-history")
    List<Integer> getSalesHistory(
            @PathVariable("productId") Long productId,
            @RequestHeader("Authorization") String token,   // We must pass the Keycloak token forward!
            @RequestHeader("X-Tenant-ID") String tenantId   // We must pass the Tenant ID forward!
    );
}