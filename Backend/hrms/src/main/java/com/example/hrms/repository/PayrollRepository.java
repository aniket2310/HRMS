package com.example.hrms.repository;

import com.example.hrms.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Optional<Payroll> findByEmployeeEmpIdAndMonthAndYear(Long empId, Integer month, Integer year);
    List<Payroll> findByMonthAndYear(Integer month, Integer year);
}
