package com.company.payroll.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.payroll.entity.Salary;
import com.company.payroll.repository.SalaryJdbcRepository;
import com.company.payroll.repository.SalaryRepository;

@Service
public class PayrollService {

    private final SalaryRepository salaryRepository;
    private final SalaryJdbcRepository salaryJdbcRepository;

    @Autowired
    public PayrollService(
            SalaryRepository salaryRepository,
            SalaryJdbcRepository salaryJdbcRepository
    ) {
        this.salaryRepository = salaryRepository;
        this.salaryJdbcRepository = salaryJdbcRepository;
    }

    public Salary generatePayroll(Salary salary) {
        // Calculate allowances and deductions (example logic)
        double allowances = salary.getBasicSalary() * 0.2; // 20% of basic
        double deductions = salary.getBasicSalary() * 0.1; // 10% of basic

        salary.setAllowances(allowances);
        salary.setDeductions(deductions);

        // Calculate net salary
        salary.setNetSalary(salary.getBasicSalary() + allowances - deductions);

        // Set createdAt and payPeriod if needed
        salary.setCreatedAt(java.time.LocalDateTime.now());
        if (salary.getPayPeriod() == null) {
            salary.setPayPeriod(java.time.LocalDate.now());
        }

        // Ensure status is set (default to PENDING if not specified)
        if (salary.getStatus() == null || salary.getStatus().isEmpty()) {
            salary.setStatus("PENDING");
        }

        System.out.println("Saving payroll with status: " + salary.getStatus());

        // Step 3: Save the payroll record
        return salaryRepository.save(salary);
    }

    // Get all payroll records for an employee (Hibernate/JPA)
    public List<Salary> getPayrollByEmployeeId(Long employeeId) {
        return salaryRepository.findByEmployeeId(employeeId);
    }

    // Get all payroll records for an employee (JDBC)
    public List<Salary> getPayrollByEmployeeIdJdbc(Long employeeId) {
        return salaryJdbcRepository.findSalariesByEmployeeId(employeeId);
    }

    // Get all payroll records
    public List<Salary> getAllPayrolls() {
        return salaryRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void deleteSalariesByEmployeeCode(String employeeCode) {
        salaryRepository.deleteByEmployeeCode(employeeCode);
    }

    // Process bulk payments for all pending payrolls
    @Transactional
    public int processBulkPayments() {
        List<Salary> allPayrolls = salaryRepository.findAllByOrderByCreatedAtDesc();
        int processedCount = 0;

        System.out.println("Total payroll records found: " + allPayrolls.size());

        for (Salary salary : allPayrolls) {
            System.out.println("Payroll ID: " + salary.getId() + ", Status: " + salary.getStatus() + ", Employee: " + salary.getEmployeeCode());
            if ("PENDING".equals(salary.getStatus())) {
                salary.setStatus("PAID");
                salaryRepository.save(salary);
                processedCount++;
                System.out.println("Processed payroll ID: " + salary.getId());
            }
        }

        System.out.println("Total processed: " + processedCount);
        return processedCount;
    }

    @Transactional
    public boolean markPayrollPaid(Long id) {
        java.util.Optional<Salary> opt = salaryRepository.findById(id);
        if (opt.isPresent()) {
            Salary salary = opt.get();
            if (!"PAID".equalsIgnoreCase(salary.getStatus())) {
                salary.setStatus("PAID");
                salaryRepository.save(salary);
                return true;
            }
        }
        return false;
    }
}
