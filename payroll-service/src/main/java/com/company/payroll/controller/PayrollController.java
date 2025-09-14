package com.company.payroll.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.payroll.entity.Salary;
import com.company.payroll.service.PayrollService;

@RestController
@RequestMapping("/api/payroll")
@PreAuthorize("hasRole('HR')") // Only HR can access all payroll operations
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    // Create a new payroll record
    @PostMapping
    public ResponseEntity<Salary> generatePayroll(@RequestBody Salary salary) {
        Salary saved = payrollService.generatePayroll(salary);
        return ResponseEntity.ok(saved);
    }

    // Get all payroll records
    @GetMapping
    public ResponseEntity<List<Salary>> getAllPayrolls() {
        List<Salary> salaries = payrollService.getAllPayrolls();
        return ResponseEntity.ok(salaries);
    }

    // Get all payroll records for an employee (Hibernate/JPA)
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Salary>> getPayrollByEmployeeId(@PathVariable Long employeeId) {
        List<Salary> salaries = payrollService.getPayrollByEmployeeId(employeeId);
        return ResponseEntity.ok(salaries);
    }

    // Get all payroll records for an employee (JDBC)
    @GetMapping("/employee/{employeeId}/jdbc")
    public ResponseEntity<List<Salary>> getPayrollByEmployeeIdJdbc(@PathVariable Long employeeId) {
        List<Salary> salaries = payrollService.getPayrollByEmployeeIdJdbc(employeeId);
        return ResponseEntity.ok(salaries);
    }

    // Delete all salary records for an employee by employeeCode (for cascade delete)
    @DeleteMapping("/salaries/by-employee/{employeeCode}")
    public ResponseEntity<String> deleteSalariesByEmployeeCode(@PathVariable String employeeCode) {
        payrollService.deleteSalariesByEmployeeCode(employeeCode);
        return ResponseEntity.ok("Salaries deleted for employee: " + employeeCode);
    }
}
