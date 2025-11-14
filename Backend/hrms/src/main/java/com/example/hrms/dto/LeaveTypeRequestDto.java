package com.example.hrms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveTypeRequestDto {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Integer maxDays;
}
