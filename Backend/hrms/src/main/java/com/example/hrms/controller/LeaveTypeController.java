package com.example.hrms.controller;

import com.example.hrms.dto.LeaveTypeRequestDto;
import com.example.hrms.dto.LeaveTypeResponseDto;
import com.example.hrms.service.LeaveTypeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave-types")
@CrossOrigin
public class LeaveTypeController {

    private final LeaveTypeService svc;

    public LeaveTypeController(LeaveTypeService svc) {
        this.svc = svc;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> create(@Valid @RequestBody LeaveTypeRequestDto dto) {
        try {
            LeaveTypeResponseDto created = svc.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", ex.getMessage())
            );
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE')")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return svc.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<Page<LeaveTypeResponseDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "leaveTypeId") String sortBy) {
        return ResponseEntity.ok(svc.list(page, size, sortBy));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody LeaveTypeRequestDto dto) {
        return svc.update(id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }
}
