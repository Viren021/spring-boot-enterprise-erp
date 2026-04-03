package com.example.erp_service_compliance.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

// @Document tells Spring to save this as a JSON object in MongoDB
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id; // MongoDB uses String IDs (e.g., "65e2a9b...")

    private String tenantId;
    private String action; // e.g., "DELETE", "UPDATE_SALARY"
    private String performedBy; // The user who did it
    private String details; // The messy JSON details of what they changed
    private LocalDateTime timestamp;

    // 🌟 THE MISSING PIECE: Store the Arbitrum Blockchain Hash
    private String web3TxHash;

    // Constructors
    public AuditLog() {}

    public AuditLog(String tenantId, String action, String performedBy, String details) {
        this.tenantId = tenantId;
        this.action = action;
        this.performedBy = performedBy;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getId() { return id; }
    public String getTenantId() { return tenantId; }
    public String getAction() { return action; }
    public String getPerformedBy() { return performedBy; }
    public String getDetails() { return details; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // 🌟 NEW: Getter and Setter so we can update the log after it hits Arbitrum
    public String getWeb3TxHash() { return web3TxHash; }
    public void setWeb3TxHash(String web3TxHash) { this.web3TxHash = web3TxHash; }
}