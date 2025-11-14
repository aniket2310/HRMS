package com.example.hrms.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRequestDto {
    public Long payrollId;         // null for create
    public Long employeeId;
    public Integer month;
    public Integer year;
    public BigDecimal basicSalary;
    public BigDecimal allowances;
    public BigDecimal deductions;
    public BigDecimal taxAmount;
}
