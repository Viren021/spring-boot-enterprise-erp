package com.example.erp_service_ai.dto;

import java.io.Serializable;
import java.util.List;

// We added 'implements Serializable' so Redis can convert this into bytes!
public record CeoDashboardResponse(
        List<Integer> productSalesTrend,
        List<Double> recentFinancials,
        Object hrRiskData
) implements Serializable {}