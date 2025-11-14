package com.example.hrms.repository;

import com.example.hrms.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    boolean existsByName(String name);
    Optional<LeaveType> findByName(String name);
}
