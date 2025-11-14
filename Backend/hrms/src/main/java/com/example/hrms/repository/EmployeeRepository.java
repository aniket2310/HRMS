package com.example.hrms.repository;

import com.example.hrms.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Page<Employee> findAll(Pageable pageable);
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByUserId(Long userId); // add this


    @Query("SELECT e.profilePhoto FROM Employee e WHERE e.empId = :empId")
    byte[] findProfilePhotoByEmpId(Long empId);

    @Query("SELECT e.aadharPdf FROM Employee e WHERE e.empId = :empId")
    byte[] findAadharPdfByEmpId(Long empId);
}
