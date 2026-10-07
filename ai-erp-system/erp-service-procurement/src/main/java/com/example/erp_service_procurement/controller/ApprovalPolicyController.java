package com.example.erp_service_procurement.controller;

import com.example.erp_service_procurement.entity.ApprovalPolicy;
import com.example.erp_service_procurement.service.ApprovalPolicyService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import com.example.erp_service_procurement.config.TenantContext;

@RestController
@RequestMapping("/api/v1/procurement/approval-policies")
public class ApprovalPolicyController {
    private final ApprovalPolicyService service;
    public ApprovalPolicyController(ApprovalPolicyService service) { this.service = service; }
    @PostMapping public ApprovalPolicy create(@RequestBody ApprovalPolicy p) { return service.save(p); }
    @GetMapping public List<ApprovalPolicy> list(@RequestParam String documentType) {
        return service.list(TenantContext.getTenantId(), documentType);
    }
    @GetMapping("/matching")
    public List<ApprovalPolicy> matching(@RequestParam String documentType,
            @RequestParam BigDecimal amount, @RequestParam(required=false) String role,
            @RequestParam(required=false) String departmentId, @RequestParam(required=false) String maker) {
        return service.matching(TenantContext.getTenantId(), documentType, amount, role, departmentId, maker);
    }
}
