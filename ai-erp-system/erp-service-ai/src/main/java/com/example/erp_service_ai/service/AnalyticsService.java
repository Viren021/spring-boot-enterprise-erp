package com.example.erp_service_ai.service;

import com.example.erp_service_ai.client.FinanceClient;
import com.example.erp_service_ai.client.HrClient;
import com.example.erp_service_ai.client.InventoryClient;
import com.example.erp_service_ai.dto.CeoDashboardResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsService {

    private final InventoryClient inventoryClient;
    private final FinanceClient financeClient;
    private final HrClient hrClient;

    public AnalyticsService(InventoryClient inventoryClient, FinanceClient financeClient, HrClient hrClient) {
        this.inventoryClient = inventoryClient;
        this.financeClient = financeClient;
        this.hrClient = hrClient;
    }

    // The key="#tenantId" ensures Tata Motors and another tenant don't see each other's cached data!
    @Cacheable(value = "ceo-dashboard", key = "#tenantId")
    public CeoDashboardResponse generateDashboard(String token, String tenantId) {

        System.out.println("⏳ REDIS MISS! Fetching fresh data from all microservices...");

        // 1. Fetch Inventory Data (Hardcoding product ID 1 for this test)
        List<Integer> salesData = inventoryClient.getSalesHistory(1L, token, tenantId);

        // 2. Fetch Finance Data
        List<Double> financeData = financeClient.getRecentTransactions(token, tenantId);

        // 3. Fetch HR Data
        Object hrData = hrClient.getEmployeeRiskData(token, tenantId);

        // 4. Combine and return!
        return new CeoDashboardResponse(salesData, financeData, hrData);
    }
}