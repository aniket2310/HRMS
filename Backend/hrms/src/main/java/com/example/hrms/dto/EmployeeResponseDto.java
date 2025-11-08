package com.example.hrms.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponseDto {
    private Long empId;
    private Long userId;
    private String firstName;
    private String lastName;
    private Long departmentId;
    private String departmentName;
    private LocalDate joiningDate;
    private String contactNumber;
    private String email;
    private String address;
    private LocalDate dob;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
