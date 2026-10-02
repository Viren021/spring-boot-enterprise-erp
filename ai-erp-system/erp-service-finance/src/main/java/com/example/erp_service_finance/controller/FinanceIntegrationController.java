package com.example.erp_service_finance.controller;

import com.example.erp_service_finance.config.TenantContext;
import com.example.erp_service_finance.dto.FinanceIntegrationEvent;
import com.example.erp_service_finance.entity.JournalEntry;
import com.example.erp_service_finance.service.FinanceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Internal service-to-service accounting contract. Gateway/service identity
 * must authenticate callers; tenantId is checked against the tenant header.
 */
@RestController
@RequestMapping("/api/v1/finance/integration")
public class FinanceIntegrationController {
    private final FinanceService financeService;

    public FinanceIntegrationController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public JournalEntry record(@RequestBody FinanceIntegrationEvent event) {
        String tenant = TenantContext.requireTenantId();
        if (event == null || event.tenantId() == null || !tenant.equals(event.tenantId())) {
            throw new IllegalArgumentException("Event tenantId must match X-Tenant-ID");
        }
        return financeService.recordIntegrationEvent(event);
    }
}
