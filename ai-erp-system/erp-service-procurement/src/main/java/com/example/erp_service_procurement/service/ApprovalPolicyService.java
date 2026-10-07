package com.example.erp_service_procurement.service;

import com.example.erp_service_procurement.entity.ApprovalPolicy;
import com.example.erp_service_procurement.repository.ApprovalPolicyRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import com.example.erp_service_procurement.config.TenantContext;

@Service
public class ApprovalPolicyService {
    private final ApprovalPolicyRepository repository;
    public ApprovalPolicyService(ApprovalPolicyRepository repository) { this.repository = repository; }
    public ApprovalPolicy save(ApprovalPolicy policy) {
        String tenant = requiredTenant();
        policy.setId(null);
        policy.setTenantId(tenant);
        if (policy.getDocumentType() == null || policy.getDocumentType().isBlank())
            throw new IllegalArgumentException("documentType is required");
        if (policy.getMinAmount() == null || policy.getMinAmount().signum() < 0)
            throw new IllegalArgumentException("minAmount cannot be negative");
        if (policy.getMaxAmount() != null && policy.getMaxAmount().compareTo(policy.getMinAmount()) < 0)
            throw new IllegalArgumentException("maxAmount cannot be below minAmount");
        return repository.save(policy);
    }
    public List<ApprovalPolicy> list(String tenant, String type) {
        return repository.findByTenantIdAndDocumentTypeAndActiveTrueOrderByApprovalLevel(tenant, type);
    }
    public List<ApprovalPolicy> matching(String tenant, String type, BigDecimal amount,
                                         String role, String department, String maker) {
        return list(tenant, type).stream().filter(p ->
                amount.compareTo(p.getMinAmount()) >= 0 &&
                (p.getMaxAmount() == null || amount.compareTo(p.getMaxAmount()) <= 0) &&
                (p.getRole() == null || p.getRole().isBlank() || p.getRole().equals(role)) &&
                (p.getDepartmentId() == null || p.getDepartmentId().isBlank() || p.getDepartmentId().equals(department)) &&
                (!p.isMakerChecker() || maker == null || !maker.equals(role))
        ).toList();
    }
    public boolean requiresApproval(String tenant, String type, BigDecimal amount,
                                    String department, String maker) {
        return list(tenant, type).stream().anyMatch(p ->
                amount.compareTo(p.getMinAmount()) >= 0 &&
                (p.getMaxAmount() == null || amount.compareTo(p.getMaxAmount()) <= 0) &&
                (p.getDepartmentId() == null || p.getDepartmentId().isBlank()
                        || p.getDepartmentId().equals(department)) &&
                p.isMakerChecker() && (maker == null || !maker.equalsIgnoreCase("system")));
    }
    private String requiredTenant() {
        String tenant = TenantContext.getTenantId();
        if (tenant == null || tenant.isBlank()) throw new IllegalStateException("Tenant context is required");
        return tenant;
    }
}
