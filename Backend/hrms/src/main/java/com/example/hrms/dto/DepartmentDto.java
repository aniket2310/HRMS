package com.example.hrms.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDto {
    private Long departmentId;
    private String name;
    private String description;
}
