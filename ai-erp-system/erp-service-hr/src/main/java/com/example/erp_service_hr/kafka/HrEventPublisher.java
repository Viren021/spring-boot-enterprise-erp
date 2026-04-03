package com.example.erp_service_hr.kafka;

import com.example.erp_service_hr.config.TenantContext;
import com.example.erp_service_hr.entity.Employee;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class HrEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public HrEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishEmployeeHiredEvent(Employee employee) {
        try {
            // 1. Convert the saved Employee object into a JSON tree
            ObjectNode eventPayload = objectMapper.valueToTree(employee);

            // 2. 🌟 INJECT THE TENANT ID! 🌟
            // Grab the current tenant (e.g., "tata_motors") from your custom context filter
            String currentTenant = TenantContext.getTenantId();
            if (currentTenant == null) {
                currentTenant = "UNKNOWN_TENANT";
            }

            // Add the fields that the Compliance service is looking for
            eventPayload.put("tenantId", currentTenant);
            eventPayload.put("action", "EMPLOYEE_HIRED");

            // 3. Convert back to String and send to the "employee-events" topic
            String jsonString = objectMapper.writeValueAsString(eventPayload);
            kafkaTemplate.send("employee-events", jsonString);

            System.out.println("📢 Broadcasted HR Event to Kafka! Topic: employee-events | Tenant: " + currentTenant);

        } catch (Exception e) {
            System.err.println("❌ Failed to publish HR event: " + e.getMessage());
        }
    }
}