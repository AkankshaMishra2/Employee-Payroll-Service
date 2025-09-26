package com.company.payroll.service;

import com.company.payroll.entity.Attendance;
import com.company.payroll.entity.Salary;
import com.company.payroll.repository.AttendanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class PayrollCalculationService {

    private static final Logger logger = LoggerFactory.getLogger(PayrollCalculationService.class);

    @Autowired
    private AttendanceRepository attendanceRepository;

    // Standard rates - these could be configured in database/properties
    private static final double STANDARD_HOURLY_RATE = 50.0;
    private static final double OVERTIME_MULTIPLIER = 1.5;
    private static final double LATE_PENALTY_AMOUNT = 25.0;
    private static final double TAX_RATE = 0.10; // 10% tax
    private static final double PF_RATE = 0.12; // 12% Provident Fund
    private static final double ESI_RATE = 0.0175; // 1.75% ESI
    private static final int STANDARD_WORKING_DAYS = 22; // Per month
    private static final double STANDARD_HOURS_PER_DAY = 8.0;

    public Salary calculateMonthlyPayroll(Long employeeId, int year, int month, List<Attendance> attendanceList) {
        logger.info("Calculating payroll for employee {} for {}/{}", employeeId, month, year);

        try {
            // Calculate attendance metrics
            AttendanceMetrics metrics = calculateAttendanceMetrics(attendanceList);

            // Calculate base salary components
            double baseSalary = calculateBaseSalary(metrics.workingDays, metrics.totalHoursWorked);
            double overtimePay = calculateOvertimePay(metrics.overtimeHours);
            double latePenalty = calculateLatePenalty(metrics.lateCount);

            // Calculate gross salary
            double grossSalary = baseSalary + overtimePay - latePenalty;

            // Calculate deductions
            double incomeTax = grossSalary * TAX_RATE;
            double providentFund = grossSalary * PF_RATE;
            double esi = grossSalary * ESI_RATE;
            double totalDeductions = incomeTax + providentFund + esi;

            // Calculate net salary
            double netSalary = grossSalary - totalDeductions;

            // Create payroll record
            Salary payroll = new Salary();
            payroll.setEmployeeId(employeeId);
            payroll.setEmployeeName(getEmployeeName(employeeId, attendanceList));
            payroll.setYear(year);
            payroll.setMonth(month);
            payroll.setPayPeriod(LocalDate.of(year, month, 1));

            // Set salary components
            payroll.setBasicSalary(baseSalary);
            payroll.setOvertimePay(overtimePay);
            payroll.setGrossSalary(grossSalary);

            // Set deductions
            payroll.setTaxDeduction(incomeTax);
            payroll.setPfDeduction(providentFund);
            payroll.setEsiDeduction(esi);
            payroll.setLatePenalty(latePenalty);
            payroll.setTotalDeductions(totalDeductions);

            // Set final amount
            payroll.setNetSalary(netSalary);

            // Set attendance metrics
            payroll.setWorkingDays((int) metrics.workingDays);
            payroll.setTotalHours(metrics.totalHoursWorked);
            payroll.setOvertimeHours(metrics.overtimeHours);
            payroll.setLateCount((int) metrics.lateCount);

            // Set processing date
            payroll.setProcessedDate(LocalDate.now());
            payroll.setStatus("PROCESSED");

            logger.info("Payroll calculated for employee {}: Gross={}, Net={}, Working Days={}",
                    employeeId, grossSalary, netSalary, metrics.workingDays);

            return payroll;

        } catch (Exception e) {
            logger.error("Error calculating payroll for employee {}: {}", employeeId, e.getMessage());
            throw new RuntimeException("Payroll calculation failed", e);
        }
    }

    private AttendanceMetrics calculateAttendanceMetrics(List<Attendance> attendanceList) {
        AttendanceMetrics metrics = new AttendanceMetrics();

        for (Attendance attendance : attendanceList) {
            if (attendance.getIsPresent()) {
                metrics.workingDays++;
                metrics.totalHoursWorked += attendance.getHoursWorked() != null ? attendance.getHoursWorked() : 0.0;
            }

            if (attendance.getIsLate() != null && attendance.getIsLate()) {
                metrics.lateCount++;
            }

            if (attendance.getOvertimeHours() != null && attendance.getOvertimeHours() > 0) {
                metrics.overtimeHours += attendance.getOvertimeHours();
            }
        }

        return metrics;
    }

    private double calculateBaseSalary(long workingDays, double totalHours) {
        // Calculate based on hours worked vs standard hours
        double standardMonthlyHours = STANDARD_WORKING_DAYS * STANDARD_HOURS_PER_DAY;
        double actualHours = Math.min(totalHours, standardMonthlyHours); // Don't count overtime in base salary

        return actualHours * STANDARD_HOURLY_RATE;
    }

    private double calculateOvertimePay(double overtimeHours) {
        return overtimeHours * STANDARD_HOURLY_RATE * OVERTIME_MULTIPLIER;
    }

    private double calculateLatePenalty(long lateCount) {
        return lateCount * LATE_PENALTY_AMOUNT;
    }

    private String getEmployeeName(Long employeeId, List<Attendance> attendanceList) {
        return attendanceList.stream()
                .map(Attendance::getEmployeeName)
                .findFirst()
                .orElse("Employee " + employeeId);
    }

    // Inner class to hold attendance metrics
    private static class AttendanceMetrics {

        long workingDays = 0;
        double totalHoursWorked = 0.0;
        double overtimeHours = 0.0;
        long lateCount = 0;
    }

    // Method to get payroll summary for an employee
    public PayrollSummary getPayrollSummary(Long employeeId, int year, int month) {
        List<Attendance> attendanceList = attendanceRepository.findByEmployeeIdAndMonth(employeeId, year, month);

        if (attendanceList.isEmpty()) {
            return null;
        }

        AttendanceMetrics metrics = calculateAttendanceMetrics(attendanceList);

        PayrollSummary summary = new PayrollSummary();
        summary.setEmployeeId(employeeId);
        summary.setYear(year);
        summary.setMonth(month);
        summary.setWorkingDays((int) metrics.workingDays);
        summary.setTotalHours(metrics.totalHoursWorked);
        summary.setOvertimeHours(metrics.overtimeHours);
        summary.setLateCount((int) metrics.lateCount);
        summary.setEstimatedGrossSalary(calculateBaseSalary(metrics.workingDays, metrics.totalHoursWorked)
                + calculateOvertimePay(metrics.overtimeHours)
                - calculateLatePenalty(metrics.lateCount));

        return summary;
    }

    // Inner class for payroll summary
    public static class PayrollSummary {

        private Long employeeId;
        private int year;
        private int month;
        private int workingDays;
        private double totalHours;
        private double overtimeHours;
        private int lateCount;
        private double estimatedGrossSalary;

        // Getters and setters
        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public int getWorkingDays() {
            return workingDays;
        }

        public void setWorkingDays(int workingDays) {
            this.workingDays = workingDays;
        }

        public double getTotalHours() {
            return totalHours;
        }

        public void setTotalHours(double totalHours) {
            this.totalHours = totalHours;
        }

        public double getOvertimeHours() {
            return overtimeHours;
        }

        public void setOvertimeHours(double overtimeHours) {
            this.overtimeHours = overtimeHours;
        }

        public int getLateCount() {
            return lateCount;
        }

        public void setLateCount(int lateCount) {
            this.lateCount = lateCount;
        }

        public double getEstimatedGrossSalary() {
            return estimatedGrossSalary;
        }

        public void setEstimatedGrossSalary(double estimatedGrossSalary) {
            this.estimatedGrossSalary = estimatedGrossSalary;
        }
    }
}
