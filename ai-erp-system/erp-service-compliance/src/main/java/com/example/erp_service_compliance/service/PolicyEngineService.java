package com.example.erp_service_compliance.service;

import com.example.erp_service_compliance.dto.PolicyCheckRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PolicyEngineService {

    public List<String> validateDocument(PolicyCheckRequest request) {
        List<String> violations = new ArrayList<>();

        // Rule Set 1: INVOICES
        if ("INVOICE".equalsIgnoreCase(request.documentType())) {

            // Rule: Must have a Tax ID
            if (!request.documentData().containsKey("taxId") || request.documentData().get("taxId").toString().isBlank()) {
                violations.add("CRITICAL: Invoice is missing a valid Tax ID (e.g., GST/VAT number).");
            }

            // Rule: High-value invoices need an approver
            if (request.documentData().containsKey("amount")) {
                double amount = Double.parseDouble(request.documentData().get("amount").toString());
                if (amount > 10000 && !request.documentData().containsKey("approvedBy")) {
                    violations.add("WARNING: Invoices over $10,000 require a secondary 'approvedBy' signature.");
                }
            }
        }

        // Rule Set 2: HR_RECORD (You can add more rules easily!)
        else if ("HR_RECORD".equalsIgnoreCase(request.documentType())) {
            if (!request.documentData().containsKey("backgroundCheckStatus")) {
                violations.add("CRITICAL: HR Record missing mandatory background check verification.");
            }
        }

        return violations; // If empty, the document is 100% compliant!
    }
}