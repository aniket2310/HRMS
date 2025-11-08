package com.example.hrms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequestDto {
    @NotBlank private String firstName;
    private String lastName;
    private LocalDate joiningDate;
    private String contactNumber;
    @Email private String email;
    private String address;
    private LocalDate dob;
    private Long departmentId; // supply department id here
}
