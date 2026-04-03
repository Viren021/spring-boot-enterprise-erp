package com.example.erp_service_ai.controller;

import com.example.erp_service_ai.client.HrClient;
import com.example.erp_service_ai.service.AiHrService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai/hr")
public class AiHrController {

    private final AiHrService aiHrService;
    private final HrClient hrClient;

    public AiHrController(AiHrService aiHrService, HrClient hrClient) {
        this.aiHrService = aiHrService;
        this.hrClient = hrClient;
    }

    @PostMapping("/attrition-risk")
    public ResponseEntity<String> checkRisk(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Tenant-ID") String tenantId) {

        System.out.println("📡 Fetching employee data from HR Service...");
        Object employeeData = hrClient.getEmployeeRiskData(token, tenantId);

        String riskReport = aiHrService.analyzeAttritionRisk(employeeData.toString());

        return ResponseEntity.ok(riskReport);
    }
}