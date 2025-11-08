package com.example.hrms.controller;

import com.example.hrms.dto.DepartmentDto;
import com.example.hrms.entity.Department;
import com.example.hrms.service.DepartmentService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin
public class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PostMapping
    public ResponseEntity<DepartmentDto> create(@Valid @RequestBody DepartmentDto dto) {
        Department d = Department.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
        Department saved = service.create(d);
        DepartmentDto resp = new DepartmentDto(saved.getDepartmentId(), saved.getName(), saved.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    // Anyone with authenticated roles can list departments (adjust as needed)
    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE')")
    @GetMapping
    public ResponseEntity<List<DepartmentDto>> list() {
        List<DepartmentDto> list = service.findAll().stream()
                .map(d -> new DepartmentDto(d.getDepartmentId(), d.getName(), d.getDescription()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDto> update(@PathVariable Long id, @Valid @RequestBody DepartmentDto dto) {
        Department updated = Department.builder().name(dto.getName()).description(dto.getDescription()).build();
        Department saved = service.update(id, updated);
        DepartmentDto resp = new DepartmentDto(saved.getDepartmentId(), saved.getName(), saved.getDescription());
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
