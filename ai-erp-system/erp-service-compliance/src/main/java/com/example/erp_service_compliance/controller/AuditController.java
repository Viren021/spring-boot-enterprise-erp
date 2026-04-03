package com.example.erp_service_compliance.controller;

import com.example.erp_service_compliance.document.AuditLog;
import com.example.erp_service_compliance.repository.AuditLogRepository;
import com.example.erp_service_compliance.blockchain.BlockchainAnchorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/compliance/audit-logs")
@CrossOrigin(origins = "http://localhost:4200")
public class AuditController {

    private final AuditLogRepository auditLogRepository;
    private final BlockchainAnchorService blockchainAnchorService;

    public AuditController(AuditLogRepository auditLogRepository, BlockchainAnchorService blockchainAnchorService) {
        this.auditLogRepository = auditLogRepository;
        this.blockchainAnchorService = blockchainAnchorService;
    }

    public record LogRequest(String action, String performedBy, String details) {}

    @PreAuthorize("hasAuthority('ROLE_COMPLIANCE_OFFICER')")
    @PostMapping
    public ResponseEntity<AuditLog> createLog(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestBody LogRequest request) {

        // 1. Create the document
        AuditLog newLog = new AuditLog(tenantId, request.action(), request.performedBy(), request.details());

        // 2. Save it initially to get the MongoDB ID
        AuditLog savedLog = auditLogRepository.save(newLog);

        // 3. 🌟 UPDATED: Send to Arbitrum in a background thread!
        blockchainAnchorService.anchorLogToArbitrum(savedLog).thenAccept(txHash -> {
            // 4. 🌟 When Arbitrum finishes, update the log with the hash and save it again!
            if (txHash != null) {
                savedLog.setWeb3TxHash(txHash);
                auditLogRepository.save(savedLog);
                System.out.println("💾 Controller: Web3 Hash saved securely to MongoDB!");
            }
        });

        // 5. Return the response immediately so the UI doesn't freeze waiting for the blockchain!
        return ResponseEntity.ok(savedLog);
    }

    @GetMapping
    public ResponseEntity<List<AuditLog>> getLogs(@RequestHeader("X-Tenant-ID") String tenantId) {
        List<AuditLog> logs = auditLogRepository.findByTenantIdOrderByTimestampDesc(tenantId);
        return ResponseEntity.ok(logs);
    }
}