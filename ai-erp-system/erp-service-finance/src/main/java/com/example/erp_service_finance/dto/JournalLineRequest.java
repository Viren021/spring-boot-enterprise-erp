package com.example.erp_service_finance.dto;
import java.math.BigDecimal;
import java.util.UUID;
public record JournalLineRequest(UUID accountId, BigDecimal debit, BigDecimal credit, String description) {}
