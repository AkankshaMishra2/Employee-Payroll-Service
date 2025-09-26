package com.company.payroll.service;

import com.company.payroll.entity.Attendance;
import com.company.payroll.entity.Salary;
import com.company.payroll.repository.AttendanceRepository;
import com.company.payroll.repository.SalaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class PayrollScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PayrollScheduler.class);

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private SalaryRepository salaryRepository;

    @Autowired
    private PayrollCalculationService payrollCalculationService;

    // Cron expression: "0 0 2 1 * ?" means 2 AM on 1st of every month
    @Scheduled(cron = "0 0 2 1 * ?")
    @Transactional
    public void processMonthlyPayroll() {
        logger.info("Starting automated monthly payroll processing...");

        try {
            // Get previous month details
            LocalDate now = LocalDate.now();
            YearMonth previousMonth = YearMonth.from(now).minusMonths(1);
            int year = previousMonth.getYear();
            int month = previousMonth.getMonthValue();

            logger.info("Processing payroll for {}/{}", month, year);

            // Get all employees who worked in previous month
            List<Long> employeeIds = attendanceRepository.findDistinctEmployeeIdsByMonth(year, month);

            if (employeeIds.isEmpty()) {
                logger.warn("No employees found for payroll processing in {}/{}", month, year);
                return;
            }

            int processedCount = 0;
            int errorCount = 0;

            for (Long employeeId : employeeIds) {
                try {
                    // Check if payroll already exists for this employee and month
                    if (salaryRepository.existsByEmployeeIdAndYearAndMonth(employeeId, year, month)) {
                        logger.info("Payroll already exists for employee {} in {}/{}", employeeId, month, year);
                        continue;
                    }

                    // Process individual employee payroll
                    Salary payroll = processEmployeePayroll(employeeId, year, month);

                    if (payroll != null) {
                        // Save payroll record
                        salaryRepository.save(payroll);
                        processedCount++;

                        logger.info("Processed payroll for employee {} - Net Salary: ${}",
                                employeeId, payroll.getNetSalary());
                    }

                } catch (Exception e) {
                    logger.error("Error processing payroll for employee {}: {}", employeeId, e.getMessage());
                    errorCount++;
                }
            }

            logger.info("Automated payroll processing completed. Successfully processed: {} employees, Errors: {}",
                    processedCount, errorCount);

        } catch (Exception e) {
            logger.error("Fatal error in automated payroll processing: {}", e.getMessage(), e);
        }
    }

    // Manual trigger for testing or emergency processing
    public void triggerManualPayrollProcessing(int year, int month) {
        logger.info("Manual payroll processing triggered for {}/{}", month, year);

        try {
            List<Long> employeeIds = attendanceRepository.findDistinctEmployeeIdsByMonth(year, month);

            for (Long employeeId : employeeIds) {
                Salary payroll = processEmployeePayroll(employeeId, year, month);
                if (payroll != null) {
                    salaryRepository.save(payroll);
                    logger.info("Manually processed payroll for employee {}", employeeId);
                }
            }

        } catch (Exception e) {
            logger.error("Error in manual payroll processing: {}", e.getMessage(), e);
            throw new RuntimeException("Manual payroll processing failed", e);
        }
    }

    private Salary processEmployeePayroll(Long employeeId, int year, int month) {
        try {
            // Get attendance data for the month
            List<Attendance> attendanceList = attendanceRepository.findByEmployeeIdAndMonth(employeeId, year, month);

            if (attendanceList.isEmpty()) {
                logger.warn("No attendance data found for employee {} in {}/{}", employeeId, month, year);
                return null;
            }

            // Calculate payroll using the calculation service
            return payrollCalculationService.calculateMonthlyPayroll(employeeId, year, month, attendanceList);

        } catch (Exception e) {
            logger.error("Error calculating payroll for employee {}: {}", employeeId, e.getMessage());
            return null;
        }
    }

    // Cron job to generate sample attendance data (for testing - can be disabled)
    @Scheduled(cron = "0 30 1 * * ?") // Daily at 1:30 AM
    public void generateSampleAttendanceData() {
        // This is for testing purposes - in real scenario, attendance would be tracked via biometric/manual entry
        logger.info("Generating sample attendance data...");

        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);

            // Sample employee IDs - adjust based on your employee data
            Long[] sampleEmployeeIds = {1L, 2L, 3L, 4L, 5L};

            for (Long employeeId : sampleEmployeeIds) {
                // Check if attendance already exists
                List<Attendance> existing = attendanceRepository.findByEmployeeIdAndAttendanceDateBetween(
                        employeeId, yesterday, yesterday);

                if (existing.isEmpty()) {
                    Attendance attendance = new Attendance();
                    attendance.setEmployeeId(employeeId);
                    attendance.setEmployeeName("Employee " + employeeId);
                    attendance.setAttendanceDate(yesterday);
                    attendance.setIsPresent(true);
                    attendance.setHoursWorked(8.0); // Standard 8-hour workday
                    attendance.setIsLate(Math.random() < 0.1); // 10% chance of being late
                    attendance.setOvertimeHours(Math.random() < 0.2 ? 2.0 : 0.0); // 20% chance of overtime

                    attendanceRepository.save(attendance);
                    logger.debug("Generated attendance for employee {} on {}", employeeId, yesterday);
                }
            }

        } catch (Exception e) {
            logger.error("Error generating sample attendance data: {}", e.getMessage());
        }
    }

    // Health check method for monitoring
    public String getSchedulerStatus() {
        return "Automated Payroll Scheduler is ACTIVE. Next monthly processing: 1st day of next month at 2:00 AM";
    }
}
