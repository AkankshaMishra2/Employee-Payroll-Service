package com.company.payroll.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping
    public ResponseEntity<List<Salary>> getAllPayrolls() {
        List<Salary> payrolls = payrollService.getAllPayrolls();
        return ResponseEntity.ok(payrolls);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Salary>> getPayrollsByEmployee(@PathVariable Long employeeId) {
        List<Salary> payrolls = payrollService.getPayrollByEmployeeId(employeeId);
        return ResponseEntity.ok(payrolls);
    }

    @PostMapping
    public ResponseEntity<Salary> createPayroll(@RequestBody Salary salary) {
        Salary savedSalary = payrollService.generatePayroll(salary);
        return ResponseEntity.ok(savedSalary);
    }

    @PostMapping("/bulk-pay")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<String> processBulkPayments() {
        try {
            int processedCount = payrollService.processBulkPayments();
            return ResponseEntity.ok("Successfully processed " + processedCount + " payroll records");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing bulk payments: " + e.getMessage());
        }
    }

    // Mark a single payroll record as PAID
    @PostMapping("/pay/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<String> paySinglePayroll(@PathVariable Long id) {
        boolean updated = payrollService.markPayrollPaid(id);
        if (updated) {
            return ResponseEntity.ok("Payroll marked as PAID");
        }
        return ResponseEntity.badRequest().body("Payroll not found or already PAID");
    }
}
