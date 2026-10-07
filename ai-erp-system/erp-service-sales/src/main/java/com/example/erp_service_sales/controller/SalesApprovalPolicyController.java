package com.example.erp_service_sales.controller;

import com.example.erp_service_sales.entity.SalesApprovalPolicy;
import com.example.erp_service_sales.service.SalesApprovalPolicyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales/approval-policies")
public class SalesApprovalPolicyController {
    private final SalesApprovalPolicyService service;
    public SalesApprovalPolicyController(SalesApprovalPolicyService service) { this.service = service; }
    @PostMapping public SalesApprovalPolicy create(@RequestBody SalesApprovalPolicy policy) { return service.save(policy); }
    @GetMapping public List<SalesApprovalPolicy> list() { return service.list(); }
}
