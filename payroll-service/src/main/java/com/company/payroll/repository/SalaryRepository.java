package com.company.payroll.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.company.payroll.entity.Salary;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long> {

    List<Salary> findByEmployeeId(Long employeeId);

    List<Salary> findByEmployeeCode(String employeeCode);

    void deleteByEmployeeCode(String employeeCode);

    List<Salary> findAllByOrderByCreatedAtDesc();
}
