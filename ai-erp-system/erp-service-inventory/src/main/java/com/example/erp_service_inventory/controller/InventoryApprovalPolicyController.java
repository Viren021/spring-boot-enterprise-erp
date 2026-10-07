package com.example.erp_service_inventory.controller;

import com.example.erp_service_inventory.entity.InventoryApprovalPolicy;
import com.example.erp_service_inventory.service.InventoryApprovalPolicyService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/approval-policies")
public class InventoryApprovalPolicyController {
    private final InventoryApprovalPolicyService service;
    public InventoryApprovalPolicyController(InventoryApprovalPolicyService service) { this.service = service; }
    @PostMapping public InventoryApprovalPolicy create(@RequestBody InventoryApprovalPolicy policy) { return service.save(policy); }
    @GetMapping public List<InventoryApprovalPolicy> list() { return service.list(); }
}
