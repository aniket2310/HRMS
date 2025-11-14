package com.example.hrms.repository;

import com.example.hrms.entity.Leave;
import com.example.hrms.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave, Long> {
    Page<Leave> findByEmployee(Employee employee, Pageable pageable);

    Page<Leave> findByStatus(String status, Pageable pageable);

    // find leaves overlapping a date range for an employee
    List<Leave> findByEmployeeAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Employee employee, LocalDate endDate, LocalDate startDate);
}
