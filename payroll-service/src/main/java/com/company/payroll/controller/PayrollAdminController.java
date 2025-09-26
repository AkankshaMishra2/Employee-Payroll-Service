package com.company.payroll.controller;

import com.company.payroll.entity.Salary;
import com.company.payroll.repository.SalaryRepository;
import com.company.payroll.service.PayrollScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class PayrollAdminController {

    @Autowired
    private PayrollScheduler payrollScheduler;

    @Autowired
    private SalaryRepository salaryRepository;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        try {
            // Get current month/year
            LocalDate now = LocalDate.now();
            int currentYear = now.getYear();
            int currentMonth = now.getMonthValue();

            // Get previous month for latest processed payroll
            YearMonth previousMonth = YearMonth.from(now).minusMonths(1);
            int prevYear = previousMonth.getYear();
            int prevMonth = previousMonth.getMonthValue();

            // Get payroll statistics
            List<Salary> currentMonthPayrolls = salaryRepository.findByYearAndMonth(currentYear, currentMonth);
            List<Salary> previousMonthPayrolls = salaryRepository.findByYearAndMonth(prevYear, prevMonth);
            Long processedCount = salaryRepository.countProcessedPayrollByMonth(prevYear, prevMonth);

            // Add data to model
            model.addAttribute("schedulerStatus", payrollScheduler.getSchedulerStatus());
            model.addAttribute("currentMonth", currentMonth);
            model.addAttribute("currentYear", currentYear);
            model.addAttribute("previousMonth", prevMonth);
            model.addAttribute("previousYear", prevYear);
            model.addAttribute("currentMonthPayrolls", currentMonthPayrolls);
            model.addAttribute("previousMonthPayrolls", previousMonthPayrolls);
            model.addAttribute("processedCount", processedCount);

            return "admin-dashboard";

        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "admin-dashboard";
        }
    }

    @PostMapping("/trigger-payroll")
    public String triggerManualPayroll(@RequestParam int year, @RequestParam int month,
            RedirectAttributes redirectAttributes) {
        try {
            payrollScheduler.triggerManualPayrollProcessing(year, month);
            redirectAttributes.addFlashAttribute("success",
                    "Payroll processing initiated for " + month + "/" + year + ". Check the logs for progress.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error triggering payroll processing: " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }
}
