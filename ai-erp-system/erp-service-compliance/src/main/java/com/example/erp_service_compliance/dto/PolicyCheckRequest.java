package com.example.erp_service_compliance.dto;

import java.util.Map;

// Using a Map lets us pass ANY JSON structure to the engine
public record PolicyCheckRequest(String documentType, Map<String, Object> documentData) {}