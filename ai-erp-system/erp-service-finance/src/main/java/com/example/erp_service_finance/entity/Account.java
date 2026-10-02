package com.example.erp_service_finance.entity;

import com.example.erp_service_finance.model.AccountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "finance_accounts",
        uniqueConstraints = @UniqueConstraint(name = "uk_finance_account_tenant_code", columnNames = {"tenant_id", "code"}))
@Getter @Setter @NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(name = "tenant_id", nullable = false, updatable = false, length = 100)
    private String tenantId;
    @Column(nullable = false, length = 30)
    private String code;
    @Column(nullable = false, length = 200)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType type;
    @Column(nullable = false)
    private boolean active = true;
}
