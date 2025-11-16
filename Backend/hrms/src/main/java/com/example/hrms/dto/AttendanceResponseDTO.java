package com.example.hrms.dto;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class AttendanceResponseDTO {

    private Long attendanceId;

    private Long empId;
    private String employeeName;

    private LocalDate date;
    private LocalTime checkIn;
    private LocalTime checkOut;

    private String status;
    private String remarks;
}