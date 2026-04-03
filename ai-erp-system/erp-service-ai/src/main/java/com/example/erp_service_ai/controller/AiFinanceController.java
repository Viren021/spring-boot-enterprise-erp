package com.example.erp_service_ai.controller;

import com.example.erp_service_ai.client.FinanceClient;
import com.example.erp_service_ai.service.AiFinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/finance")
public class AiFinanceController {

    private final AiFinanceService aiFinanceService;
    private final FinanceClient financeClient;

    public AiFinanceController(AiFinanceService aiFinanceService, FinanceClient financeClient) {
        this.aiFinanceService = aiFinanceService;
        this.financeClient = financeClient;
    }

    @PostMapping("/anomaly-check")
    public ResponseEntity<String> checkAnomalies(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Tenant-ID") String tenantId) {

        // 1. Fetch data from Finance Service using Feign
        System.out.println("📡 Fetching ledger data from Finance Service...");
        List<Double> transactions = financeClient.getRecentTransactions(token, tenantId);

        // 2. Pass the fetched data to our AI Auditor
        String auditReport = aiFinanceService.detectAnomalies(transactions);

        return ResponseEntity.ok(auditReport);
    }
}