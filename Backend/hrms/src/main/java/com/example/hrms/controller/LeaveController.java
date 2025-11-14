package com.example.hrms.controller;

import com.example.hrms.dto.LeaveRequestDto;
import com.example.hrms.dto.LeaveResponseDto;
import com.example.hrms.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
@CrossOrigin
public class LeaveController {

    private final LeaveService svc;

    public LeaveController(LeaveService svc) {
        this.svc = svc;
    }

    // Employees can create for themselves (omit employeeId) ; Admin/HR can create for any employee
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE')")
    public ResponseEntity<?> create(@Valid @RequestBody LeaveRequestDto dto, Authentication auth) {
        try {
            LeaveResponseDto resp = svc.create(dto, auth);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE')")
    public ResponseEntity<?> getById(@PathVariable Long id, Authentication auth) {
        return svc.getById(id).map(dto -> ResponseEntity.ok(dto))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // List with optional filters. Admin/HR can list all; employees could list own by setting employeeId=their id (or omit)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE')")
    public ResponseEntity<Page<LeaveResponseDto>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo
    ) {
        LocalDate from = dateFrom != null ? LocalDate.parse(dateFrom) : null;
        LocalDate to = dateTo != null ? LocalDate.parse(dateTo) : null;
        return ResponseEntity.ok(svc.list(page, size, sortBy, employeeId, status, from, to));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody LeaveRequestDto dto, Authentication auth) {
        return svc.update(id, dto, auth)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete / cancel
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE')")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        try {
            svc.delete(id, auth);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    // Approve / Reject
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> changeStatus(@PathVariable Long id, @RequestParam String status, Authentication auth) {
        try {
            var updated = svc.changeStatus(id, status, auth);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
