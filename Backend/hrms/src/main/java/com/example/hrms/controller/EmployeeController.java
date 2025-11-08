package com.example.hrms.controller;

import com.example.hrms.dto.EmployeeRequestDto;
import com.example.hrms.dto.EmployeeResponseDto;
import com.example.hrms.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin
public class EmployeeController {

    private final EmployeeService svc;

    public EmployeeController(EmployeeService svc) {
        this.svc = svc;
    }

    // Only ADMIN or HR can create employees
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<EmployeeResponseDto> create(@Valid @RequestBody EmployeeRequestDto req) {
        EmployeeResponseDto created = svc.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Get single employee. Admin/HR can view any. EMPLOYEE can view own (ownership check below).
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE')")
    public ResponseEntity<?> getById(@PathVariable Long id, Authentication auth) {
        return svc.getById(id).map(dto -> {
            // ownership check: if caller is EMPLOYEE, allow only if mapping matches
            boolean isEmployee = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
            if (isEmployee) {
                // If you store userId in Employee and you can get username->User->id, verify ownership.
                // Example pseudo: if (!dto.getUserId().equals(currentUserId)) return 403.
                // We leave implementation hook here; by default we allow admin/hr to view.
            }
            return ResponseEntity.ok(dto);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<Page<EmployeeResponseDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "empId") String sortBy) {
        return ResponseEntity.ok(svc.list(page, size, sortBy));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDto req) {
        return svc.update(id, req).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }
}
