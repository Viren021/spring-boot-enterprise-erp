package com.example.erp_service_hr.kafka;

import com.example.erp_service_hr.config.TenantContext;
import com.example.erp_service_hr.entity.Employee;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

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

        public void publishPayrollApprovedEvent(com.example.erp_service_hr.entity.PayrollRun run) {
            if (run.getNetAmount() == null || run.getNetAmount().signum() <= 0)
                throw new IllegalStateException("Cannot publish PAYROLL_APPROVED without a positive netAmount");
            String tenant = TenantContext.getTenantId();
            if (tenant == null || tenant.isBlank()) throw new IllegalStateException("Tenant context is required");
            String document = String.valueOf(run.getId());
            String id = UUID.nameUUIDFromBytes((tenant + ":PAYROLL_APPROVED:" + document)
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
            try {
                kafkaTemplate.send("finance-integration-events", objectMapper.writeValueAsString(Map.of(
                        "eventId", id, "tenantId", tenant, "eventType", "PAYROLL_APPROVED",
                        "sourceDocumentId", document, "amount", run.getNetAmount(),
                        "eventDate", LocalDate.now(), "description", "Payroll run " + document,
                        "direction", "DEBIT")));
            } catch (Exception e) { throw new IllegalStateException("Unable to publish payroll finance event", e); }
        }
}