package com.example.erp_service_finance.dto;
import java.time.LocalDate;
import java.util.List;
public record JournalEntryRequest(String entryNumber, LocalDate entryDate, String description,
                                  List<JournalLineRequest> lines) {}
