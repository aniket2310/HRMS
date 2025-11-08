package com.example.hrms.service;

import com.example.hrms.entity.Department;
import com.example.hrms.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {
    private final DepartmentRepository repo;

    public DepartmentService(DepartmentRepository repo) {
        this.repo = repo;
    }

    public Department create(Department dept) {
        return repo.save(dept);
    }

    public List<Department> findAll() {
        return repo.findAll();
    }

    public Optional<Department> findById(Long id) {
        return repo.findById(id);
    }

    public Department update(Long id, Department updated) {
        return repo.findById(id).map(d -> {
            d.setName(updated.getName());
            d.setDescription(updated.getDescription());
            return repo.save(d);
        }).orElseThrow(() -> new RuntimeException("Department not found"));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
