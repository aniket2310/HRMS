package com.example.hrms.service;

import com.example.hrms.dto.EmployeeRequestDto;
import com.example.hrms.dto.EmployeeResponseDto;
import com.example.hrms.entity.Department;
import com.example.hrms.entity.Employee;
import com.example.hrms.repository.DepartmentRepository;
import com.example.hrms.repository.EmployeeRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository repo;
    private final DepartmentRepository departmentRepository;

    public EmployeeService(EmployeeRepository repo, DepartmentRepository departmentRepository) {
        this.repo = repo;
        this.departmentRepository = departmentRepository;
    }

    public EmployeeResponseDto create(EmployeeRequestDto req) {
        Department dept = null;
        if (req.getDepartmentId() != null) {
            dept = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        }

        Employee e = Employee.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .joiningDate(req.getJoiningDate())
                .contactNumber(req.getContactNumber())
                .email(req.getEmail())
                .address(req.getAddress())
                .dob(req.getDob())
                .department(dept)
                .status("Active")
                .build();
        Employee saved = repo.save(e);
        return toDto(saved);
    }

    public Optional<EmployeeResponseDto> getById(Long id) {
        return repo.findById(id).map(this::toDto);
    }

    public Page<EmployeeResponseDto> list(int page, int size, String sortBy) {
        Pageable p = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        return repo.findAll(p).map(this::toDto);
    }

    public Optional<EmployeeResponseDto> update(Long id, EmployeeRequestDto req) {
        return repo.findById(id).map(emp -> {
            emp.setFirstName(req.getFirstName());
            emp.setLastName(req.getLastName());
            emp.setContactNumber(req.getContactNumber());
            emp.setEmail(req.getEmail());
            emp.setAddress(req.getAddress());
            emp.setDob(req.getDob());
            emp.setJoiningDate(req.getJoiningDate());
            if (req.getDepartmentId() != null) {
                Department dept = departmentRepository.findById(req.getDepartmentId())
                        .orElseThrow(() -> new IllegalArgumentException("Department not found"));
                emp.setDepartment(dept);
            } else {
                emp.setDepartment(null);
            }
            Employee updated = repo.save(emp);
            return toDto(updated);
        });
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private EmployeeResponseDto toDto(Employee e) {
        Long deptId = e.getDepartment() != null ? e.getDepartment().getDepartmentId() : null;
        String deptName = e.getDepartment() != null ? e.getDepartment().getName() : null;
        return EmployeeResponseDto.builder()
                .empId(e.getEmpId())
                .userId(e.getUserId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .departmentId(deptId)
                .departmentName(deptName)
                .joiningDate(e.getJoiningDate())
                .contactNumber(e.getContactNumber())
                .email(e.getEmail())
                .address(e.getAddress())
                .dob(e.getDob())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
