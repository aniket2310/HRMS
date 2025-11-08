package com.example.hrms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterUserDto {
    @NotBlank @Size(min = 3, max = 50)
    private String username;

    @NotBlank @Size(min = 6, max = 100)
    private String password;

    @NotBlank @Email
    private String email;

    // Only honored when caller is ADMIN
    private Set<String> roles;
}
