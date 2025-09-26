package com.company.payroll.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.company.payroll.entity.Salary;
import com.company.payroll.service.PayrollService;

@Controller
public class PayrollWebController {

    @Autowired
    private PayrollService payrollService;

    @GetMapping("/")
    public String payrollServiceHome(Model model) {
        try {
            long totalRecords = payrollService.getAllPayrolls().size();
            model.addAttribute("totalRecords", totalRecords);
            model.addAttribute("serviceName", "Payroll Service");
            model.addAttribute("port", "8082");
            model.addAttribute("status", "Running");
        } catch (Exception e) {
            model.addAttribute("totalRecords", 0);
            model.addAttribute("serviceName", "Payroll Service");
            model.addAttribute("port", "8082");
            model.addAttribute("status", "Error: " + e.getMessage());
        }
        return "payroll-home";
    }

    // Show all payroll records
    @GetMapping("/payrolls")
    public String showPayrolls(Model model) {
        List<Salary> payrolls = payrollService.getAllPayrolls();
        model.addAttribute("payrolls", payrolls);
        return "payrolls";
    }

    // Show payroll form for adding new payroll
    @GetMapping("/payrolls/new")
    public String showPayrollForm(Model model) {
        model.addAttribute("salary", new Salary());
        model.addAttribute("isEdit", false);
        return "payroll_form";
    }

    // Show payroll form for editing
    @GetMapping("/payrolls/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        List<Salary> payrolls = payrollService.getAllPayrolls();
        Salary salary = payrolls.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (salary != null) {
            model.addAttribute("salary", salary);
            model.addAttribute("isEdit", true);
            return "payroll_form";
        }
        return "redirect:/payrolls";
    }

    // Release all pending payrolls (web action)
    @GetMapping("/payrolls/release-all")
    public String releaseAllPayrolls() {
        int processed = payrollService.processBulkPayments();
        return "redirect:/payrolls?released=" + processed;
    }

    // Save payroll (create or update)
    @PostMapping("/payrolls/save")
    public String savePayroll(@ModelAttribute Salary salary) {
        if (salary.getPayPeriod() == null) {
            salary.setPayPeriod(LocalDate.now());
        }
        // Calculate net salary
        double net = (salary.getBasicSalary() != null ? salary.getBasicSalary() : 0.0)
                + (salary.getAllowances() != null ? salary.getAllowances() : 0.0)
                - (salary.getDeductions() != null ? salary.getDeductions() : 0.0);
        salary.setNetSalary(net);

        payrollService.generatePayroll(salary);
        return "redirect:/payrolls";
    }

    // Delete payroll
    @GetMapping("/payrolls/delete/{employeeCode}")
    public String deletePayroll(@PathVariable String employeeCode) {
        payrollService.deleteSalariesByEmployeeCode(employeeCode);
        return "redirect:/payrolls";
    }

    // Release/pay a single payroll record (web action)
    @GetMapping("/payrolls/pay/{id}")
    public String payPayroll(@PathVariable Long id) {
        boolean updated = payrollService.markPayrollPaid(id);
        return "redirect:/payrolls?released=" + (updated ? 1 : 0);
    }

    // Show payrolls by employee
    @GetMapping("/payrolls/employee/{employeeId}")
    public String showPayrollsByEmployee(@PathVariable Long employeeId, Model model) {
        List<Salary> payrolls = payrollService.getPayrollByEmployeeId(employeeId);
        model.addAttribute("payrolls", payrolls);
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("filterTitle", "Employee ID " + employeeId + " Payroll Records");
        return "payrolls";
    }

    // Serve custom login page
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
