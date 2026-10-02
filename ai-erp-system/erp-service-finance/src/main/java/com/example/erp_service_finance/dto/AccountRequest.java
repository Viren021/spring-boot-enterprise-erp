package com.example.erp_service_finance.dto;
import com.example.erp_service_finance.model.AccountType;
public record AccountRequest(String code, String name, AccountType type) {}
