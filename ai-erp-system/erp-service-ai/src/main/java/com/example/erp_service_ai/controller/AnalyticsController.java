package com.example.erp_service_ai.controller;

import com.example.erp_service_ai.dto.CeoDashboardResponse;
import com.example.erp_service_ai.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<CeoDashboardResponse> getDashboard(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Tenant-ID") String tenantId) {

        CeoDashboardResponse dashboard = analyticsService.generateDashboard(token, tenantId);
        return ResponseEntity.ok(dashboard);
    }
}