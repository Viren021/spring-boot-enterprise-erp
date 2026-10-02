package com.example.erp_service_finance.controller;

import com.example.erp_service_finance.dto.*;
import com.example.erp_service_finance.entity.*;
import com.example.erp_service_finance.service.FinanceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/finance")
public class FinanceController {
    private final FinanceService service;
    public FinanceController(FinanceService service) { this.service = service; }
    @GetMapping("/accounts") public List<Account> accounts() { return service.accounts(); }
    @PostMapping("/accounts") @ResponseStatus(HttpStatus.CREATED)
    public Account createAccount(@RequestBody AccountRequest request) { return service.createAccount(request); }
    @GetMapping("/periods") public List<FiscalPeriod> periods() { return service.periods(); }
    @PostMapping("/periods") @ResponseStatus(HttpStatus.CREATED)
    public FiscalPeriod createPeriod(@RequestBody FiscalPeriodRequest request) { return service.createPeriod(request); }
    @GetMapping("/journals") public List<JournalEntry> journals() { return service.journals(); }
    @PostMapping("/journals") @ResponseStatus(HttpStatus.CREATED)
    public JournalEntry createJournal(@RequestBody JournalEntryRequest request) { return service.createJournal(request); }
    @PostMapping("/journals/{id}/post") public JournalEntry post(@PathVariable UUID id) { return service.postJournal(id); }
}
