package com.example.erp_service_hr.controller;

import com.example.erp_service_hr.entity.Employee;
import com.example.erp_service_hr.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import this!
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr/employees")
@CrossOrigin(origins = "http://localhost:4200")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    // 🔒 ONLY HR ADMINS CAN ADD EMPLOYEES
    @PreAuthorize("hasAuthority('ROLE_HR_ADMIN')")
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.createEmployee(employee));
    }

    // 🔓 EVERYONE CAN READ THE LIST (Needed for Dashboard)
    @GetMapping
    public ResponseEntity<List<Employee>> getEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // --- AI RISK DATA ---
    public record EmployeeRiskData(String name, int yearsAtCompany, int monthsSinceLastPromotion, int sickDaysTaken) {}

    // 🔓 EVERYONE CAN READ RISK DATA (Needed for AI Service)
    @GetMapping("/risk-data")
    public ResponseEntity<List<EmployeeRiskData>> getEmployeeRiskData() {
        List<EmployeeRiskData> data = List.of(
                new EmployeeRiskData("Alice", 5, 12, 2),
                new EmployeeRiskData("Bob", 3, 36, 15),
                new EmployeeRiskData("Charlie", 1, 6, 0)
        );
        return ResponseEntity.ok(data);
    }
}