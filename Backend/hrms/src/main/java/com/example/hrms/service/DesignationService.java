package com.example.hrms.service;

import com.example.hrms.dto.DesignationRequestDto;
import com.example.hrms.dto.DesignationResponseDto;
import java.util.List;

public interface DesignationService {
    DesignationResponseDto createDesignation(DesignationRequestDto dto);
    List<DesignationResponseDto> getAllDesignations();
    DesignationResponseDto getDesignationById(Long id);
    DesignationResponseDto updateDesignation(Long id, DesignationRequestDto dto);
    void deleteDesignation(Long id);
}
