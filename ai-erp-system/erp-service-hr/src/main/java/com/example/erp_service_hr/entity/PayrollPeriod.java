package com.example.erp_service_hr.entity;
import jakarta.persistence.*; import lombok.Data; import java.time.LocalDate;
@Entity @Data @Table(name="payroll_periods")
public class PayrollPeriod {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private LocalDate startDate; private LocalDate endDate; private LocalDate paymentDate;
 @Enumerated(EnumType.STRING) private ApprovalStatus status=ApprovalStatus.PENDING;
}
