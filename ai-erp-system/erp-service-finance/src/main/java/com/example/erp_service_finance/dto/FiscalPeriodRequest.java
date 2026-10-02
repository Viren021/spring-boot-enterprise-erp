package com.example.erp_service_finance.dto;
import java.time.LocalDate;
public record FiscalPeriodRequest(String name, LocalDate startDate, LocalDate endDate) {}
