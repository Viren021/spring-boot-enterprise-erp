package com.example.erp_tenant_service.controller;


import com.example.erp_tenant_service.entity.Tenant;
import com.example.erp_tenant_service.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @PostMapping
    public ResponseEntity<?> onboardNewCompany(@RequestParam String companyName) {
        try {
            Tenant newTenant = tenantService.createTenant(companyName);
            return ResponseEntity.ok(newTenant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating tenant: " + e.getMessage());
        }
    }

    @GetMapping("/{tenantId}/config")
    public ResponseEntity<?> getTenantConfig(@PathVariable String tenantId) {
        // tenantRepository comes from the TenantService, so let's use the service
        return tenantService.getTenantById(tenantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
