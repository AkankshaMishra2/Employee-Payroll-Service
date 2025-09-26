package com.company.payroll.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.company.payroll.entity.Salary;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long> {

    List<Salary> findByEmployeeId(Long employeeId);

    List<Salary> findByEmployeeCode(String employeeCode);

    void deleteByEmployeeCode(String employeeCode);

    List<Salary> findAllByOrderByCreatedAtDesc();

    // New methods for automated payroll processing
    boolean existsByEmployeeIdAndYearAndMonth(Long employeeId, Integer year, Integer month);

    List<Salary> findByEmployeeIdAndYearAndMonth(Long employeeId, Integer year, Integer month);

    @Query("SELECT s FROM Salary s WHERE s.year = :year AND s.month = :month ORDER BY s.createdAt DESC")
    List<Salary> findByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);

    @Query("SELECT COUNT(s) FROM Salary s WHERE s.year = :year AND s.month = :month AND s.status = 'PROCESSED'")
    Long countProcessedPayrollByMonth(@Param("year") Integer year, @Param("month") Integer month);
}
