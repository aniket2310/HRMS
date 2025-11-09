package com.example.hrms.service.impl;

import com.example.hrms.dto.DesignationRequestDto;
import com.example.hrms.dto.DesignationResponseDto;
import com.example.hrms.entity.Department;
import com.example.hrms.entity.Designation;
import com.example.hrms.repository.DepartmentRepository;
import com.example.hrms.repository.DesignationRepository;
import com.example.hrms.service.DesignationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public DesignationResponseDto createDesignation(DesignationRequestDto dto) {
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Department not found with ID: " + dto.getDepartmentId()));

        Designation designation = Designation.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .level(dto.getLevel())
                .department(department)
                .build();

        designation = designationRepository.save(designation);
        return mapToResponse(designation);
    }

    @Override
    public List<DesignationResponseDto> getAllDesignations() {
        return designationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DesignationResponseDto getDesignationById(Long id) {
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Designation not found with ID: " + id));
        return mapToResponse(designation);
    }

    @Override
    public DesignationResponseDto updateDesignation(Long id, DesignationRequestDto dto) {
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Designation not found with ID: " + id));

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Department not found with ID: " + dto.getDepartmentId()));

        designation.setName(dto.getName());
        designation.setDescription(dto.getDescription());
        designation.setLevel(dto.getLevel());
        designation.setDepartment(department);

        designationRepository.save(designation);
        return mapToResponse(designation);
    }

    @Override
    public void deleteDesignation(Long id) {
        if (!designationRepository.existsById(id)) {
            throw new EntityNotFoundException("Designation not found with ID: " + id);
        }
        designationRepository.deleteById(id);
    }

    private DesignationResponseDto mapToResponse(Designation designation) {
        return DesignationResponseDto.builder()
                .designationId(designation.getDesignationId())
                .name(designation.getName())
                .description(designation.getDescription())
                .level(designation.getLevel())
                .departmentName(designation.getDepartment().getName())
                .build();
    }
}
