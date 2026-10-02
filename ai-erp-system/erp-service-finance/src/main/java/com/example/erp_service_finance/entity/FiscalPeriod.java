package com.example.erp_service_finance.entity;

import com.example.erp_service_finance.model.PeriodStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "finance_fiscal_periods")
@Getter @Setter @NoArgsConstructor
public class FiscalPeriod {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", nullable = false, updatable = false, length = 100) private String tenantId;
    @Column(nullable = false, length = 100) private String name;
    @Column(nullable = false) private LocalDate startDate;
    @Column(nullable = false) private LocalDate endDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private PeriodStatus status = PeriodStatus.OPEN;
}
