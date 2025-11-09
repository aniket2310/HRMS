package com.example.hrms.repository;

import com.example.hrms.entity.Designation;
import com.example.hrms.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DesignationRepository extends JpaRepository<Designation, Long> {
    Optional<Designation> findByName(String name);
    List<Designation> findByDepartment(Department department);
}
