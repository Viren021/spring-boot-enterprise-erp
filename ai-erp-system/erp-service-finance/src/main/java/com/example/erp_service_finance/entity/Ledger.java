package com.example.erp_service_finance.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "ledgers")
public class Ledger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    private Double amount;

    @Column(name = "transaction_type") // <-- THIS IS THE FIX!
    private String transactionType;
}