package com.example.erp_service_ai.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

// Tell Spring to route these requests to the Finance Service on port 8084!
@FeignClient(name = "finance-service", url = "http://localhost:8084")
public interface FinanceClient {

    @GetMapping("/api/v1/finance/ledger/recent")
    List<Double> getRecentTransactions(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Tenant-ID") String tenantId
    );
}