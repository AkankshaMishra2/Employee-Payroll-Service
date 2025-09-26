package com.company.payroll.repository;

import com.company.payroll.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Find attendance records for a specific employee
    List<Attendance> findByEmployeeIdAndAttendanceDateBetween(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    // Find attendance for a specific month
    @Query("SELECT a FROM Attendance a WHERE a.employeeId = :employeeId "
            + "AND YEAR(a.attendanceDate) = :year AND MONTH(a.attendanceDate) = :month")
    List<Attendance> findByEmployeeIdAndMonth(
            @Param("employeeId") Long employeeId,
            @Param("year") int year,
            @Param("month") int month);

    // Calculate total working days for an employee in a month
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employeeId = :employeeId "
            + "AND a.isPresent = true AND YEAR(a.attendanceDate) = :year AND MONTH(a.attendanceDate) = :month")
    Long countWorkingDaysByEmployeeAndMonth(
            @Param("employeeId") Long employeeId,
            @Param("year") int year,
            @Param("month") int month);

    // Calculate total hours worked in a month
    @Query("SELECT COALESCE(SUM(a.hoursWorked), 0) FROM Attendance a WHERE a.employeeId = :employeeId "
            + "AND YEAR(a.attendanceDate) = :year AND MONTH(a.attendanceDate) = :month")
    Double sumHoursWorkedByEmployeeAndMonth(
            @Param("employeeId") Long employeeId,
            @Param("year") int year,
            @Param("month") int month);

    // Calculate total overtime hours in a month
    @Query("SELECT COALESCE(SUM(a.overtimeHours), 0) FROM Attendance a WHERE a.employeeId = :employeeId "
            + "AND YEAR(a.attendanceDate) = :year AND MONTH(a.attendanceDate) = :month")
    Double sumOvertimeHoursByEmployeeAndMonth(
            @Param("employeeId") Long employeeId,
            @Param("year") int year,
            @Param("month") int month);

    // Count late arrivals in a month
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employeeId = :employeeId "
            + "AND a.isLate = true AND YEAR(a.attendanceDate) = :year AND MONTH(a.attendanceDate) = :month")
    Long countLateArrivalsByEmployeeAndMonth(
            @Param("employeeId") Long employeeId,
            @Param("year") int year,
            @Param("month") int month);

    // Find all employees who worked in a specific month (for bulk payroll processing)
    @Query("SELECT DISTINCT a.employeeId FROM Attendance a WHERE "
            + "YEAR(a.attendanceDate) = :year AND MONTH(a.attendanceDate) = :month")
    List<Long> findDistinctEmployeeIdsByMonth(@Param("year") int year, @Param("month") int month);
}
