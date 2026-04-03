package com.example.erp_service_inventory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory")
@CrossOrigin(origins = "http://localhost:4200")
public class StatsController {

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getInventoryStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalItems", 5240);
        stats.put("lowStockAlerts", 12);
        stats.put("recentRestocks", 34);

        // 🌟 ADD THIS LINE: This is exactly what Angular is waiting for!
        stats.put("sales", new int[]{100, 120, 110, 130, 150, 180});

        return ResponseEntity.ok(stats);
    }
}