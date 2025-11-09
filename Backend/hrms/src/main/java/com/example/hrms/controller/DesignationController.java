package com.example.hrms.controller;

import com.example.hrms.dto.DesignationRequestDto;
import com.example.hrms.dto.DesignationResponseDto;
import com.example.hrms.service.DesignationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/designations")
@Tag(name = "Designation Management")
@CrossOrigin
public class DesignationController {

    private final DesignationService designationService;

    public DesignationController(DesignationService designationService) {
        this.designationService = designationService;
    }

    @PostMapping
    public ResponseEntity<DesignationResponseDto> create(@Valid @RequestBody DesignationRequestDto dto) {
        return ResponseEntity.ok(designationService.createDesignation(dto));
    }

    @GetMapping
    public ResponseEntity<List<DesignationResponseDto>> getAll() {
        return ResponseEntity.ok(designationService.getAllDesignations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DesignationResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(designationService.getDesignationById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DesignationResponseDto> update(@PathVariable Long id,
                                                         @Valid @RequestBody DesignationRequestDto dto) {
        return ResponseEntity.ok(designationService.updateDesignation(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        designationService.deleteDesignation(id);
        return ResponseEntity.ok("Deleted successfully");
    }
}
