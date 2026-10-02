package com.example.erp_service_hr.entity;
import jakarta.persistence.*; import lombok.Data; import java.math.BigDecimal; import java.time.LocalDateTime;
@Entity @Data @Table(name="payroll_runs")
public class PayrollRun {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private Long periodId; private Long employeeId; private BigDecimal grossAmount; private BigDecimal deductions; private BigDecimal netAmount;
 @Enumerated(EnumType.STRING) private ApprovalStatus status=ApprovalStatus.PENDING; private LocalDateTime processedAt;
}
