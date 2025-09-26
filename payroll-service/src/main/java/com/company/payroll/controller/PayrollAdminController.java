package com.company.payroll.controller;

import com.company.payroll.entity.Salary;
import com.company.payroll.repository.SalaryRepository;
import com.company.payroll.service.PayrollScheduler;
import com.company.payroll.service.PayrollPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class PayrollAdminController {

    @Autowired
    private PayrollScheduler payrollScheduler;

    @Autowired
    private SalaryRepository salaryRepository;

    @Autowired
    private PayrollPdfService payrollPdfService;

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
            List<Salary> allPayrolls = salaryRepository.findAllByOrderByCreatedAtDesc();
            Long processedCount = salaryRepository.countProcessedPayrollByMonth(prevYear, prevMonth);

            // Add data to model
            model.addAttribute("schedulerStatus", payrollScheduler.getSchedulerStatus());
            model.addAttribute("currentMonth", currentMonth);
            model.addAttribute("currentYear", currentYear);
            model.addAttribute("previousMonth", prevMonth);
            model.addAttribute("previousYear", prevYear);
            model.addAttribute("currentMonthPayrolls", currentMonthPayrolls);
            model.addAttribute("previousMonthPayrolls", previousMonthPayrolls);
            model.addAttribute("allPayrolls", allPayrolls);
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

    // PDF Export Endpoints
    /**
     * Export individual payslip as PDF
     */
    @GetMapping("/export-pdf/payslip/{id}")
    public ResponseEntity<byte[]> exportPayslipPdf(@PathVariable Long id) {
        try {
            Optional<Salary> salaryOpt = salaryRepository.findById(id);
            if (salaryOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Salary salary = salaryOpt.get();
            byte[] pdfBytes = payrollPdfService.generatePayslipPdf(salary);

            String filename = "payslip_" + salary.getEmployeeCode() + "_"
                    + salary.getMonth() + "-" + salary.getYear() + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Export all payroll records as bulk PDF report
     */
    @GetMapping("/export-pdf/bulk")
    public ResponseEntity<byte[]> exportBulkPayrollReport() {
        try {
            List<Salary> allSalaries = salaryRepository.findAllByOrderByCreatedAtDesc();

            if (allSalaries.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            byte[] pdfBytes = payrollPdfService.generateBulkPayrollReport(allSalaries, "All Payroll Records Report");

            String filename = "bulk_payroll_report_" + LocalDate.now().toString() + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Export current month payroll records as PDF report
     */
    @GetMapping("/export-pdf/current-month")
    public ResponseEntity<byte[]> exportCurrentMonthReport() {
        try {
            LocalDate now = LocalDate.now();
            int currentYear = now.getYear();
            int currentMonth = now.getMonthValue();

            List<Salary> currentMonthSalaries = salaryRepository.findByYearAndMonth(currentYear, currentMonth);

            if (currentMonthSalaries.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            String[] months = {"", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
            String title = months[currentMonth] + " " + currentYear + " Payroll Report";

            byte[] pdfBytes = payrollPdfService.generateBulkPayrollReport(currentMonthSalaries, title);

            String filename = "payroll_report_" + currentMonth + "-" + currentYear + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Export payroll records by custom date range
     */
    @GetMapping("/export-pdf/date-range")
    public ResponseEntity<byte[]> exportByDateRange(
            @RequestParam int fromYear, @RequestParam int fromMonth,
            @RequestParam int toYear, @RequestParam int toMonth) {
        try {
            // For now, we'll just export the specific month requested
            // In a full implementation, you'd query multiple months in the range
            List<Salary> salaries = salaryRepository.findByYearAndMonth(fromYear, fromMonth);

            if (salaries.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            String title = "Payroll Report (" + fromMonth + "/" + fromYear + " - " + toMonth + "/" + toYear + ")";
            byte[] pdfBytes = payrollPdfService.generateBulkPayrollReport(salaries, title);

            String filename = "payroll_report_" + fromMonth + "-" + fromYear + "_to_" + toMonth + "-" + toYear + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
