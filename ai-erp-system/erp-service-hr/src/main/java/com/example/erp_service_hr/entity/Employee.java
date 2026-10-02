package com.example.erp_service_hr.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String department;
    private Double salary;
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private Long managerId;
    private String jobTitle;
    private LocalDate dateOfBirth;
    @Version
    private Long version;
}