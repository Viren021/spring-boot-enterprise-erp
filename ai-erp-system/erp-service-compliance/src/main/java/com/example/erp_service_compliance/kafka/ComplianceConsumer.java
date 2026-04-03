package com.example.erp_service_compliance.kafka;

import com.example.erp_service_compliance.blockchain.BlockchainAnchorService;
import com.example.erp_service_compliance.document.AuditLog;
import com.example.erp_service_compliance.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ComplianceConsumer {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final BlockchainAnchorService blockchainAnchorService;

    public ComplianceConsumer(AuditLogRepository auditLogRepository,
                              ObjectMapper objectMapper,
                              BlockchainAnchorService blockchainAnchorService) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
        this.blockchainAnchorService = blockchainAnchorService;
    }

    @KafkaListener(topics = {"order-events", "payment-events", "employee-events"}, groupId = "compliance-audit-group")
    public void consumeAnyEvent(ConsumerRecord<String, String> record) {
        System.out.println("\n==========================================");
        System.out.println("🕵️ COMPLIANCE AUDIT: Intercepted Event!");

        String rawJson = record.value();
        String extractedTenantId = "UNKNOWN";

        try {
            JsonNode jsonNode = objectMapper.readTree(rawJson);
            if (jsonNode.has("tenantId")) {
                extractedTenantId = jsonNode.get("tenantId").asText();
            }
        } catch (Exception e) {
            System.out.println("⚠️ Could not extract tenantId: " + e.getMessage());
        }

        String action = "KAFKA_SAGA_" + record.topic().toUpperCase();
        String performedBy = "SYSTEM_BROKER";

        AuditLog newLog = new AuditLog(
                extractedTenantId,
                action,
                performedBy,
                rawJson
        );

        // 3. Save it to MongoDB immediately so Kafka is happy!
        AuditLog savedLog = auditLogRepository.save(newLog);

        System.out.println("✅ Saved to MongoDB Audit Vault!");
        System.out.println("Tenant: " + savedLog.getTenantId() + " | Action: " + savedLog.getAction());

        // 🌟 4. UPDATED: FIRE IT TO THE BLOCKCHAIN (IN THE BACKGROUND)!
        blockchainAnchorService.anchorLogToArbitrum(savedLog).thenAccept(web3Hash -> {
            if (web3Hash != null) {
                // When Web3 finishes (could be seconds later), quietly update the MongoDB record with the hash
                savedLog.setWeb3TxHash(web3Hash);
                auditLogRepository.save(savedLog);
                System.out.println("💾 Web3 Hash saved securely to MongoDB!");
            }
        });
    }
}