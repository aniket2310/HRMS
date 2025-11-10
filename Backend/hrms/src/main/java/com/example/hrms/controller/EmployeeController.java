package com.example.hrms.controller;

import com.example.hrms.dto.EmployeeRequestDto;
import com.example.hrms.dto.EmployeeResponseDto;
import com.example.hrms.service.EmployeeService;
import com.example.hrms.service.EmployeeService.FileData;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.io.IOException;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin
public class EmployeeController {

    private final EmployeeService svc;

    public EmployeeController(EmployeeService svc) {
        this.svc = svc;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<EmployeeResponseDto> create(@Valid @RequestBody EmployeeRequestDto req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(svc.create(req));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE')")
    public ResponseEntity<?> getById(@PathVariable Long id, Authentication auth) {
        return svc.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
    public ResponseEntity<EmployeeResponseDto> update(@PathVariable Long id,
                                                      @Valid @RequestBody EmployeeRequestDto req) {
        var opt = svc.update(id, req);            // Optional<EmployeeResponseDto>
        if (opt.isPresent()) {
            EmployeeResponseDto dto = opt.get();
            // optional: log dto for debug
            System.out.println("update success: " + dto); // replace with logger
            return ResponseEntity.ok(dto);
        } else {
            System.out.println("update not found for id: " + id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- FILE ENDPOINTS ----------

    @PutMapping(value = "/{id}/profile-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> uploadProfilePhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            svc.uploadProfilePhoto(id, file);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/profile-photo")
    public ResponseEntity<?> downloadProfilePhoto(@PathVariable Long id) {
        try {
            FileData file = svc.downloadProfilePhoto(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.contentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.filename() + "\"")
                    .body(file.data());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/profile-photo")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> deleteProfilePhoto(@PathVariable Long id) {
        svc.deleteProfilePhoto(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}/aadhar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> uploadAadhar(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            svc.uploadAadharPdf(id, file);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/aadhar")
    public ResponseEntity<?> downloadAadhar(@PathVariable Long id) {
        try {
            FileData file = svc.downloadAadharPdf(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.filename() + "\"")
                    .body(file.data());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/aadhar")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> deleteAadhar(@PathVariable Long id) {
        svc.deleteAadharPdf(id);
        return ResponseEntity.noContent().build();
    }
}
