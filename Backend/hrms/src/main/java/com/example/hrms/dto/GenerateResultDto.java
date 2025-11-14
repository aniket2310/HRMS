package com.example.hrms.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateResultDto {
    private PayrollResponseDto payroll; // result object (if single)
    private List<String> warnings;      // validation warnings
    private boolean persisted;          // true if saved as GENERATED
}
