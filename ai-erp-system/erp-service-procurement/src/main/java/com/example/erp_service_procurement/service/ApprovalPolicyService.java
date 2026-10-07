package com.example.erp_service_procurement.service;

import com.example.erp_service_procurement.entity.ApprovalPolicy;
import com.example.erp_service_procurement.repository.ApprovalPolicyRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ApprovalPolicyService {
    private final ApprovalPolicyRepository repository;
    public ApprovalPolicyService(ApprovalPolicyRepository repository) { this.repository = repository; }
    public ApprovalPolicy save(ApprovalPolicy policy) { return repository.save(policy); }
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
}
