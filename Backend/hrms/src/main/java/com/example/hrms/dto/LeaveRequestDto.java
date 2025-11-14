package com.example.hrms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestDto {
    // if admin/HR creates on behalf of an employee they can supply employeeId.
    // if null, the server will use the authenticated user's employee record (for EMPLOYEE role).
    private Long employeeId;

    @NotNull
    private Long leaveTypeId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private String reason;
}
