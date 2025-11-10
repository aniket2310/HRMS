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
    @NotBlank
    private String firstName;
    private String lastName;
    private LocalDate joiningDate;
    private String contactNumber;

    @Email
    private String email;
    private String address;
    private LocalDate dob;
    private Long departmentId;

    private Boolean removeProfilePhoto = false;
    private Boolean removeAadharPdf = false;

    // NEW: optional credentials for creating a linked User
    // If username is provided, password must be provided as well (checked in service)
    private String username;
    private String password;
}
