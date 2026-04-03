package com.example.erp_service_hr.repository;

import com.example.erp_service_hr.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // We get findAll(), save(), findById() for free!
}