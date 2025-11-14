package com.example.hrms.dto;

import com.example.hrms.entity.Payroll.PayrollStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollResponseDto {
    private Long payrollId;
    private Long employeeId;
    private String employeeName;
    private Integer month;
    private Integer year;
    private BigDecimal basicSalary;
    private BigDecimal allowances;
    private BigDecimal deductions;
    private BigDecimal taxAmount;
    private BigDecimal netSalary;
    private PayrollStatus status;
    private LocalDateTime createdAt;
}
