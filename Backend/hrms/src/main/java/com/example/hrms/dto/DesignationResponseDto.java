package com.example.hrms.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignationResponseDto {
    private Long designationId;
    private String name;
    private String description;
    private Integer level;
    private String departmentName; // ✅ show only name, not full object
}
