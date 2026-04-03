package com.example.erp_service_compliance.controller;

import com.example.erp_service_compliance.dto.PolicyCheckRequest;
import com.example.erp_service_compliance.service.PolicyEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/compliance/policy")
public class PolicyController {

    private final PolicyEngineService policyEngineService;

    public PolicyController(PolicyEngineService policyEngineService) {
        this.policyEngineService = policyEngineService;
    }

    @PreAuthorize("hasAuthority('ROLE_COMPLIANCE_OFFICER')")
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkPolicy(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestBody PolicyCheckRequest request) {

        System.out.println("🛡️ Running compliance check on document type: " + request.documentType());

        List<String> violations = policyEngineService.validateDocument(request);

        // If the violations list is empty, it passes!
        boolean isCompliant = violations.isEmpty();

        return ResponseEntity.ok(Map.of(
                "compliant", isCompliant,
                "violations", violations,
                "documentType", request.documentType()
        ));
    }
}