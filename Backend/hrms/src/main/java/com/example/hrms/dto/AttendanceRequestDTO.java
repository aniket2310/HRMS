package com.example.hrms.dto;
import lombok.Data;

@Data
public class AttendanceRequestDTO {
    private String status;     // Present / Absent / Leave
    private String remarks;    // optional
}
