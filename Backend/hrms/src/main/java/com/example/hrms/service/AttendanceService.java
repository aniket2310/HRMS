package com.example.hrms.service;

import com.example.hrms.dto.AttendanceRequestDTO;
import com.example.hrms.dto.AttendanceResponseDTO;

import java.util.List;

public interface AttendanceService {

    AttendanceResponseDTO checkIn(Long empId);

    AttendanceResponseDTO checkOut(Long empId);

    List<AttendanceResponseDTO> getAttendanceByEmployee(Long empId);

    List<AttendanceResponseDTO> getAllAttendance();

    List<AttendanceResponseDTO> getAttendanceByDate(String date);

    AttendanceResponseDTO updateStatus(Long attendanceId, AttendanceRequestDTO dto);
}
