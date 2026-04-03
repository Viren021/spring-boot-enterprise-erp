package com.example.erp_service_compliance.controller;

import com.example.erp_service_compliance.client.HrClient;
import com.example.erp_service_compliance.document.AuditLog;
import com.example.erp_service_compliance.repository.AuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/compliance/gdpr")
public class GdprController {

    private final HrClient hrClient;
    private final AuditLogRepository auditLogRepository;

    public GdprController(HrClient hrClient, AuditLogRepository auditLogRepository) {
        this.hrClient = hrClient;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/export/{employeeName}")
    public ResponseEntity<Map<String, Object>> exportUserData(
            @PathVariable String employeeName,
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Tenant-ID") String tenantId) {

        System.out.println("⚖️ GDPR Request Received for: " + employeeName);

        // 1. Fetch HR Data via Feign
        List<Object> allHrData = hrClient.getAllEmployeeData(token, tenantId);

        // (In a real app, we would filter by ID, but we will filter by name for this test)
        Object userHrProfile = allHrData.stream()
                .filter(data -> data.toString().contains(employeeName))
                .findFirst()
                .orElse("No HR Data Found");

        // 2. Fetch Audit Logs involving this user from MongoDB
        List<AuditLog> userLogs = auditLogRepository.findByTenantIdOrderByTimestampDesc(tenantId)
                .stream()
                .filter(log -> log.getDetails().contains(employeeName) || log.getPerformedBy().equals(employeeName))
                .collect(Collectors.toList());

        // 3. Compile the final GDPR Dossier
        Map<String, Object> gdprDossier = Map.of(
                "legalRequestOrigin", "Right to Access (GDPR Article 15)",
                "subject", employeeName,
                "hrProfile", userHrProfile,
                "systemActivityLogs", userLogs
        );

        return ResponseEntity.ok(gdprDossier);
    }
}