package com.example.erp_service_compliance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "hr-service", url = "http://localhost:8082")
public interface HrClient {

    // Re-using the endpoint we built yesterday!
    @GetMapping("/api/v1/hr/employees/risk-data")
    List<Object> getAllEmployeeData(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Tenant-ID") String tenantId
    );
}