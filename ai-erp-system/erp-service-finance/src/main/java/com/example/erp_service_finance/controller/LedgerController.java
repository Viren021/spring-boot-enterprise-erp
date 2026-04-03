package com.example.erp_service_finance.controller;

import com.example.erp_service_finance.model.Transaction;
import com.example.erp_service_finance.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/finance/ledger")
@CrossOrigin(origins = "http://localhost:4200")
public class LedgerController {

    @Autowired
    private TransactionRepository transactionRepository;

    // 🔓 READ: Fetch real-time data directly from PostgreSQL
    @GetMapping("/recent")
    public ResponseEntity<List<Double>> getRecentTransactions() {

        // Fetch all transactions from the database, extract just the amounts, and return as a list
        List<Double> amounts = transactionRepository.findAll().stream()
                .map(Transaction::getAmount)
                .collect(Collectors.toList());

        // Fallback: If the database is completely empty, send a default anomaly so the UI still looks cool
        if (amounts.isEmpty()) {
            amounts.add(50000.00);
        }

        return ResponseEntity.ok(amounts);
    }

    // 🔒 WRITE: Save directly to the database
    @PostMapping("/add")
    @PreAuthorize("hasRole('FINANCE_ADMIN')")
    public ResponseEntity<String> addTransaction(@RequestBody Transaction request) {

        // Spring Data JPA automatically writes this to your PostgreSQL table!
        transactionRepository.save(request);

        return ResponseEntity.ok("✅ Successfully saved " + request.getType() + " of $" + request.getAmount() + " to the PostgreSQL database.");
    }
}