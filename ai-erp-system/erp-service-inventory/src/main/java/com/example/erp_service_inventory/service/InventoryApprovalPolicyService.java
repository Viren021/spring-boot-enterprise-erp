package com.example.erp_service_inventory.service;

import com.example.erp_service_inventory.config.TenantContext;
import com.example.erp_service_inventory.entity.InventoryApprovalPolicy;
import com.example.erp_service_inventory.repository.InventoryApprovalPolicyRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class InventoryApprovalPolicyService {
    private final InventoryApprovalPolicyRepository repository;
    public InventoryApprovalPolicyService(InventoryApprovalPolicyRepository repository) { this.repository = repository; }
    public InventoryApprovalPolicy save(InventoryApprovalPolicy policy) {
        policy.setId(null); policy.setTenantId(requiredTenant());
        if (policy.getOperation() == null || policy.getOperation().isBlank())
            throw new IllegalArgumentException("operation is required");
        if (policy.getMinAmount() == null || policy.getMinAmount().signum() < 0)
            throw new IllegalArgumentException("minAmount cannot be negative");
        return repository.save(policy);
    }
    public List<InventoryApprovalPolicy> list() {
        return repository.findByTenantIdAndOperationAndActiveTrue(requiredTenant(), "STOCK_ADJUSTMENT");
    }
    public boolean requiresApproval(String operation, BigDecimal amount, String actor) {
        if (actor == null || actor.isBlank() || "system".equalsIgnoreCase(actor)) return false;
        return repository.findByTenantIdAndOperationAndActiveTrue(requiredTenant(), operation).stream()
                .anyMatch(p -> p.isMakerChecker() && amount.compareTo(p.getMinAmount()) >= 0);
    }
    private String requiredTenant() {
        String tenant = TenantContext.getTenantId();
        if (tenant == null || tenant.isBlank()) throw new IllegalStateException("X-Tenant-ID is required");
        return tenant;
    }
}
