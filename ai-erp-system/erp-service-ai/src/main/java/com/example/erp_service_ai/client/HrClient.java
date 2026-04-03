package com.example.erp_service_ai.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

// Assuming your HR service runs on Port 8082
@FeignClient(name = "hr-service", url = "http://localhost:8082")
public interface HrClient {

    @GetMapping("/api/v1/hr/employees/risk-data")
    Object getEmployeeRiskData(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Tenant-ID") String tenantId
    );
}