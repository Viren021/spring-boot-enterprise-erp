package com.example.erp_service_inventory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.erp_service_inventory.config.TenantContext;
import com.example.erp_service_inventory.repository.ProductRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory")
@CrossOrigin(origins = "http://localhost:4200")
public class StatsController {
    private final ProductRepository products;

    public StatsController(ProductRepository products) {
        this.products = products;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getInventoryStats() {
        Map<String, Object> stats = new HashMap<>();
        String tenant = TenantContext.getTenantId();
        if (tenant == null || tenant.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        var activeProducts = products.findByTenantIdAndActiveTrue(tenant);
        stats.put("totalItems", activeProducts.stream()
                .mapToLong(product -> product.getStockQuantity() == null ? 0 : product.getStockQuantity()).sum());
        stats.put("lowStockAlerts", activeProducts.stream()
                .filter(product -> product.getStockQuantity() != null
                        && product.getReorderLevel() != null
                        && product.getStockQuantity() <= product.getReorderLevel())
                .count());
        stats.put("recentRestocks", 0);
        stats.put("sales", java.util.List.of());

        return ResponseEntity.ok(stats);
    }
}