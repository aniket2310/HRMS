package com.example.hrms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignationRequestDto {

    @NotBlank
    private String name;

    private String description;

    private Integer level;

    @NotNull
    private Long departmentId; // ✅ only the departmentId (not full department)
}
