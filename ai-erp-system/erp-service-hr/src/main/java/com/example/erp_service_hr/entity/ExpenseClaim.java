package com.example.erp_service_hr.entity;
import jakarta.persistence.*; import lombok.Data; import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Data @Table(name="expense_claims")
public class ExpenseClaim {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private Long employeeId; private LocalDate expenseDate; private String description; private String category;
 private BigDecimal amount; private String currency="USD"; private String receiptUrl;
 @Enumerated(EnumType.STRING) private ApprovalStatus status=ApprovalStatus.PENDING;
 private String approvedBy; private String rejectionReason;
}
