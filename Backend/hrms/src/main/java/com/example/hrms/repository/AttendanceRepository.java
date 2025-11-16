package com.example.hrms.repository;

import com.example.hrms.entity.Attendance;
import com.example.hrms.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeAndDate(Employee employee, LocalDate date);

    List<Attendance> findAllByEmployee(Employee employee);

    List<Attendance> findAllByDate(LocalDate date);

    List<Attendance> findAllByEmployee_EmpId(Long empId);
}
