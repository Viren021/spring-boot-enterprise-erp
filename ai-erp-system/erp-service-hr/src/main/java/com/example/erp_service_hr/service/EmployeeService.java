package com.example.erp_service_hr.service;

import com.example.erp_service_hr.entity.Employee;
import com.example.erp_service_hr.repository.EmployeeRepository;
import com.example.erp_service_hr.kafka.HrEventPublisher; // 🌟 IMPORT NEW PUBLISHER
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDate;
import com.example.erp_service_hr.entity.EmployeeStatus;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private HrEventPublisher hrEventPublisher; // 🌟 INJECT THE PUBLISHER

    // The TenantContext and ConnectionProvider will automatically
    // route this to the correct schema (e.g., tenant_tata_motors)
    @Transactional
    public Employee createEmployee(Employee employee) {
        // 1. Save to the isolated PostgreSQL schema
        Employee savedEmployee = employeeRepository.save(employee);

        // 2. 📢 Broadcast to Kafka that a new hire just happened!
        hrEventPublisher.publishEmployeeHiredEvent(savedEmployee);

        // 3. Return the saved entity back to the controller
        return savedEmployee;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Transactional
    public Employee updateLifecycle(Long id, EmployeeStatus status, LocalDate terminationDate) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
        if (status == EmployeeStatus.TERMINATED && terminationDate == null) terminationDate = LocalDate.now();
        if (status != EmployeeStatus.TERMINATED) terminationDate = null;
        employee.setStatus(status);
        employee.setTerminationDate(terminationDate);
        return employeeRepository.save(employee);
    }
}